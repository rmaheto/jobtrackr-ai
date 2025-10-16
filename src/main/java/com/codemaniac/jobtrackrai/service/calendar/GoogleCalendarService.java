package com.codemaniac.jobtrackrai.service.calendar;

import com.codemaniac.jobtrackrai.annotation.EnsureGoogleAccessTokenFresh;
import com.codemaniac.jobtrackrai.entity.FollowUp;
import com.codemaniac.jobtrackrai.entity.User;
import com.codemaniac.jobtrackrai.repository.FollowUpRepository;
import com.codemaniac.jobtrackrai.service.CurrentUserService;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleCalendarService implements CalendarIntegrationService {

  private final CurrentUserService currentUserService;
  private final FollowUpRepository followUpRepository;

  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
  private static final String PRIMARY = "primary";
  private static final String GOOGLE = "google";
  private static final String CREATED_BY_APP = "createdByApp";
  private static final String JOB_TRACKR_APP = "jobTrackrApp";
  private static final String START_TIME = "startTime";

  @Value("${google.calendar.scope}")
  private String calendarScope;

  @Value("${google.calendar.app-name}")
  private String appName;

  @EnsureGoogleAccessTokenFresh
  public Event addEvent(
      final String summary, final String description, final Instant start, final Instant end)
      throws IOException, GeneralSecurityException {

    final User user = requireGoogleUser();
    final Calendar service = buildCalendarClient(user);
    final String userTimeZone = getUserCalendarTimeZone(service);

    final EventDateTime startTime =
        new EventDateTime()
            .setDateTime(new DateTime(start.toEpochMilli()))
            .setTimeZone(userTimeZone);

    final EventDateTime endTime =
        new EventDateTime().setDateTime(new DateTime(end.toEpochMilli())).setTimeZone(userTimeZone);

    final Event event =
        new Event()
            .setSummary(summary)
            .setDescription(description)
            .setExtendedProperties(
                new Event.ExtendedProperties()
                    .setPrivate(
                        Map.of(
                            CREATED_BY_APP, JOB_TRACKR_APP,
                            "sourceEntity", "FollowUp")))
            .setSource(
                new Event.Source().setTitle(JOB_TRACKR_APP).setUrl("https://jobtrackrpro.com"))
            .setStart(startTime)
            .setEnd(endTime);

    final Event created = service.events().insert(PRIMARY, event).execute();
    log.info(
        "Created JobTrackrAI event: {} ({}) [{}]",
        created.getSummary(),
        created.getId(),
        userTimeZone);
    return created;
  }

  @EnsureGoogleAccessTokenFresh
  @Override
  public List<Event> getEventsInRange(final Instant start, final Instant end)
      throws IOException, GeneralSecurityException {

    final User user = requireGoogleUser();
    final Calendar service = buildCalendarClient(user);

    // Fetch only events created by JobTrackrAI
    final Events events =
        service
            .events()
            .list(PRIMARY)
            .setTimeMin(new DateTime(start.toEpochMilli()))
            .setTimeMax(new DateTime(end.toEpochMilli()))
            .setSingleEvents(true)
            .setOrderBy(START_TIME)
            .execute();

    final List<Event> allItems = events.getItems();

    final List<String> jobTrackrEventIds =
        followUpRepository.findAllByJobApplication_User_Id(user.getId()).stream()
            .map(FollowUp::getCalendarEventId)
            .filter(Objects::nonNull)
            .toList();

    final List<Event> filtered =
        allItems.stream()
            .filter(
                e -> {
                  if (e.getExtendedProperties() != null
                      && e.getExtendedProperties().getPrivate() != null) {
                    final String tag = e.getExtendedProperties().getPrivate().get(CREATED_BY_APP);
                    if (JOB_TRACKR_APP.equalsIgnoreCase(tag)) return true;
                  }
                  return jobTrackrEventIds.contains(e.getId());
                })
            .toList();

    log.info(
        "Fetched {} JobTrackrAI events between {} and {} for {}",
        filtered.size(),
        start,
        end,
        user.getEmail());

    return filtered;
  }

  @EnsureGoogleAccessTokenFresh
  @Override
  public List<Event> getUpcomingEvents(final int maxResults)
      throws IOException, GeneralSecurityException {

    final User user = requireGoogleUser();
    final Calendar service = buildCalendarClient(user);

    final DateTime now = new DateTime(System.currentTimeMillis());
    final Events events =
        service
            .events()
            .list(PRIMARY)
            .setMaxResults(maxResults)
            .setTimeMin(now)
            .setOrderBy(START_TIME)
            .setSingleEvents(true)
            .execute();

    final List<Event> items = events.getItems();
    log.info("Fetched {} upcoming events for {}", items.size(), user.getEmail());
    return items;
  }

  @EnsureGoogleAccessTokenFresh
  public Event updateEvent(
      final String eventId, final String newSummary, final String newDescription, final Instant start, final Instant end)
      throws IOException, GeneralSecurityException {

    final User user = requireGoogleUser();
    final Calendar service = buildCalendarClient(user);

    final Event existing = service.events().get(PRIMARY, eventId).execute();
    if (newSummary != null) existing.setSummary(newSummary);
    if (newDescription != null) existing.setDescription(newDescription);
    if(start != null) existing.setStart(new EventDateTime().setDateTime(new DateTime(start.toEpochMilli())));
    if(end != null) existing.setEnd(new EventDateTime().setDateTime(new DateTime(end.toEpochMilli())));

    final Event updated = service.events().update(PRIMARY, existing.getId(), existing).execute();
    log.info("Updated calendar event: {} ({})", updated.getSummary(), updated.getId());
    return updated;
  }

  @EnsureGoogleAccessTokenFresh
  @Override
  public void deleteEvent(final String eventId) throws IOException, GeneralSecurityException {

    final User user = requireGoogleUser();
    final Calendar service = buildCalendarClient(user);

    service.events().delete(PRIMARY, eventId).execute();
    log.info("Deleted calendar event with ID: {}", eventId);
  }

  @EnsureGoogleAccessTokenFresh
  public void deleteOldEvents(final int daysBack) throws IOException, GeneralSecurityException {

    final User user = requireGoogleUser();
    final Calendar service = buildCalendarClient(user);

    final DateTime cutoff =
        new DateTime(Instant.now().minus(daysBack, ChronoUnit.DAYS).toEpochMilli());
    final Events events =
        service.events().list(PRIMARY).setTimeMax(cutoff).setSingleEvents(true).execute();

    for (final Event e : events.getItems()) {
      service.events().delete(PRIMARY, e.getId()).execute();
      log.info("Deleted old event: {} ({})", e.getSummary(), e.getId());
    }
  }

  @Override
  public void disconnectUser() {
    final var user = currentUserService.getCurrentUser();
    if (user.getGoogleAccessToken() != null) {
      log.info("Disconnecting Google Calendar for {}", user.getEmail());
      user.setGoogleAccessToken(null);
      user.setGoogleRefreshToken(null);
      user.setTokenExpiry(null);
    }
  }

  @Override
  public String getProvider() {
    return GOOGLE;
  }

  private User requireGoogleUser() {
    final User user = currentUserService.getCurrentUser();
    if (user == null || user.getGoogleAccessToken() == null) {
      throw new IllegalStateException("User is not connected to Google Calendar");
    }
    if (!GOOGLE.equalsIgnoreCase(user.getProvider())) {
      throw new IllegalStateException("Only Google users can access Google Calendar");
    }
    return user;
  }

  private Calendar buildCalendarClient(final User user)
      throws IOException, GeneralSecurityException {

    final HttpRequestInitializer credentials =
        new HttpCredentialsAdapter(
            GoogleCredentials.create(new AccessToken(user.getGoogleAccessToken(), null))
                .createScoped(Collections.singletonList(calendarScope)));

    return new Calendar.Builder(
            GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credentials)
        .setApplicationName(appName)
        .build();
  }

  private String getUserCalendarTimeZone(final Calendar service) throws IOException {
    final CalendarListEntry primary = service.calendarList().get(PRIMARY).execute();
    return primary.getTimeZone();
  }
}

package com.codemaniac.jobtrackrai.service.calendar;

import com.google.api.services.calendar.model.Event;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.List;

/**
 * Unified contract for calendar providers (Google, Outlook, etc.). Each implementation wraps its
 * provider’s API behind a common interface.
 */
public interface CalendarIntegrationService {

  String getProvider();

  Event addEvent(String summary, String description, Instant start, Instant end)
      throws IOException, GeneralSecurityException;

  List<Event> getEventsInRange(Instant start, Instant end)
      throws IOException, GeneralSecurityException;

  List<Event> getUpcomingEvents(int maxResults) throws IOException, GeneralSecurityException;

  Event updateEvent(String eventId, String summary, String description, Instant start, Instant end)
      throws IOException, GeneralSecurityException;

  void deleteEvent(String eventId) throws IOException, GeneralSecurityException;

  default void disconnectUser() {
    // default no-op, overridden by providers that support disconnect
  }
}

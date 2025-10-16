package com.codemaniac.jobtrackrai.service;

import com.codemaniac.jobtrackrai.dto.FollowUpDto;
import com.codemaniac.jobtrackrai.dto.FollowUpRequest;
import com.codemaniac.jobtrackrai.entity.FollowUp;
import com.codemaniac.jobtrackrai.entity.JobApplication;
import com.codemaniac.jobtrackrai.exception.NotFoundException;
import com.codemaniac.jobtrackrai.mapper.FollowUpMapper;
import com.codemaniac.jobtrackrai.repository.FollowUpRepository;
import com.codemaniac.jobtrackrai.repository.JobApplicationRepository;
import com.codemaniac.jobtrackrai.service.calendar.CalendarIntegrationManager;
import com.codemaniac.jobtrackrai.service.calendar.CalendarIntegrationService;
import com.google.api.services.calendar.model.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FollowUpServiceImpl implements FollowUpService {

  private final FollowUpRepository followUpRepository;
  private final JobApplicationRepository jobApplicationRepository;
  private final FollowUpMapper followUpMapper;
  private final CalendarIntegrationManager calendarManager;

  @Transactional
  public FollowUpDto scheduleFollowUp(final Long applicationId, final FollowUpRequest request) {
    final JobApplication jobApplication =
        jobApplicationRepository
            .findById(applicationId)
            .orElseThrow(() -> new NotFoundException(applicationId));

    final FollowUp followUp =
        FollowUp.builder()
            .scheduledAt(request.getScheduledAt())
            .type(request.getType())
            .notes(request.getNotes())
            .jobApplication(jobApplication)
            .completed(false)
            .build();

    final FollowUp saved = followUpRepository.save(followUp);
    createCalendarEventIfPossible(saved, jobApplication);
    return followUpMapper.toDto(saved);
  }

  @Transactional(readOnly = true)
  public List<FollowUpDto> getFollowUps(final Long applicationId) {
    return followUpRepository.findByJobApplicationId(applicationId).stream()
        .map(followUpMapper::toDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<FollowUpDto> findFollowUpsBetween(
      final OffsetDateTime from, final OffsetDateTime to) {
    return followUpRepository.findByScheduledAtBetween(from, to).stream()
        .map(followUpMapper::toDto)
        .toList();
  }

  @Transactional
  public FollowUpDto updateFollowUp(final Long followUpId, final FollowUpRequest request) {
    final FollowUp followUp =
        followUpRepository.findById(followUpId)
            .orElseThrow(() -> new NotFoundException(followUpId));

    // Update basic details
    followUp.setType(request.getType());
    followUp.setNotes(request.getNotes());
    followUp.setScheduledAt(request.getScheduledAt());

    final FollowUp saved = followUpRepository.save(followUp);

    // If it’s linked to an external calendar, update there too
    updateCalendarEventIfLinked(saved);

    return followUpMapper.toDto(saved);
  }

  @Transactional
  public FollowUpDto markCompleted(final Long followUpId) {
    final FollowUp followUp =
        followUpRepository
            .findById(followUpId)
            .orElseThrow(() -> new NotFoundException(followUpId));
    followUp.setCompleted(true);
    return followUpMapper.toDto(followUpRepository.save(followUp));
  }

  public void deleteFollowUp(final Long followUpId) {
    final FollowUp followUp =
        followUpRepository
            .findById(followUpId)
            .orElseThrow(() -> new NotFoundException(followUpId));

    // Remove from Calendar if linked
    if (followUp.getCalendarEventId() != null) {
      try {
        final CalendarIntegrationService calendar = calendarManager.getCurrentUserCalendar();
        calendar.deleteEvent(followUp.getCalendarEventId());
        log.info(
            "Deleted {} event {} for follow-up {}",
            followUp.getCalendarProvider(),
            followUp.getCalendarEventId(),
            followUpId);
      } catch (final Exception e) {
        log.warn(
            "Failed to delete {} event for follow-up {}: {}",
            followUp.getCalendarProvider(),
            followUpId,
            e.getMessage());
      }
    }

    followUpRepository.delete(followUp);
  }

  private void createCalendarEventIfPossible(
      final FollowUp followUp, final JobApplication jobApplication) {
    try {
      final CalendarIntegrationService calendar = calendarManager.getCurrentUserCalendar();

      final Instant start = followUp.getScheduledAt().toInstant();
      final Instant end = start.plusSeconds(1800);

      final String summary = followUp.getType() + " • " + jobApplication.getCompany();
      final String description =
          (followUp.getNotes() != null ? followUp.getNotes() + "\n\n" : "")
              + "Follow-up for "
              + jobApplication.getRole()
              + " at "
              + jobApplication.getCompany();

      final Event event = calendar.addEvent(summary, description, start, end);
      followUp.setCalendarEventId(event.getId());
      followUp.setCalendarProvider(calendar.getProvider());
      followUpRepository.save(followUp);

      log.info(
          "Created {} Calendar event for follow-up: {}", calendar.getProvider(), event.getId());
    } catch (final Exception e) {
      log.warn("Failed to create Calendar event for follow-up: {}", e.getMessage());
    }
  }

  private void updateCalendarEventIfLinked(final FollowUp followUp) {
    if (followUp.getCalendarEventId() == null) {
      log.debug("No external calendar event linked for follow-up {}", followUp.getId());
      return;
    }

    try {
      final CalendarIntegrationService calendar = calendarManager.getCurrentUserCalendar();
      if (calendar == null) {
        log.debug("No calendar integration active for user");
        return;
      }

      final Instant start = followUp.getScheduledAt().toInstant();
      final Instant end = start.plusSeconds(1800);

      final String summary =
          followUp.getType() + " • " + followUp.getJobApplication().getCompany();
      final String description =
          (followUp.getNotes() != null ? followUp.getNotes() + "\n\n" : "")
              + "Follow-up for "
              + followUp.getJobApplication().getRole()
              + " at "
              + followUp.getJobApplication().getCompany();

      calendar.updateEvent(followUp.getCalendarEventId(), summary, description, start, end);
      log.info(
          "Updated {} calendar event {} for follow-up {}",
          followUp.getCalendarProvider(),
          followUp.getCalendarEventId(),
          followUp.getId());
    } catch (final Exception e) {
      log.warn("Failed to update linked event for follow-up {}: {}", followUp.getId(), e.getMessage());
    }
  }
}

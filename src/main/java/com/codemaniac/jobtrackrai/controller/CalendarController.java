package com.codemaniac.jobtrackrai.controller;

import com.codemaniac.jobtrackrai.model.ApiResponse;
import com.codemaniac.jobtrackrai.service.calendar.CalendarIntegrationManager;
import com.google.api.services.calendar.model.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/calendar")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("isAuthenticated()")
public class CalendarController {

  private final CalendarIntegrationManager calendarManager;

  @GetMapping("/events")
  public ResponseEntity<ApiResponse<List<Event>>> getEventsInRange(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final Instant from,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final Instant to) {

    try {
      final var events = calendarManager.getCurrentUserCalendar().getEventsInRange(from, to);
      return ResponseEntity.ok(ApiResponse.of("OK", "Calendar events retrieved", events));
    } catch (final Exception e) {
      log.error("Failed to fetch calendar events: {}", e.getMessage(), e);
      return ResponseEntity.internalServerError()
          .body(ApiResponse.of("ERROR", "Failed to fetch calendar events", null));
    }
  }

  @PostMapping("/connect")
  public ResponseEntity<ApiResponse<String>> connectCalendar() {
    // This endpoint mainly serves as a confirmation placeholder.
    return ResponseEntity.ok(
        ApiResponse.of("OK", "Google Calendar connected via OAuth2", "connected"));
  }

  @PostMapping("/disconnect")
  public ResponseEntity<ApiResponse<String>> disconnectCalendar() {
    try {
      calendarManager.disconnectCurrentUserCalendar();
      return ResponseEntity.ok(ApiResponse.of("OK", "Calendar disconnected", "disconnected"));
    } catch (final Exception e) {
      log.error("Failed to disconnect calendar: {}", e.getMessage(), e);
      return ResponseEntity.internalServerError()
          .body(ApiResponse.of("ERROR", "Failed to disconnect calendar", null));
    }
  }
}

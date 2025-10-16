package com.codemaniac.jobtrackrai.service.calendar;

import com.codemaniac.jobtrackrai.entity.User;
import com.codemaniac.jobtrackrai.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CalendarIntegrationManager {

  private final List<CalendarIntegrationService> calendarServices;
  private final CurrentUserService currentUserService;

  public CalendarIntegrationService getCurrentUserCalendar() {
    final User user = currentUserService.getCurrentUser();
    return calendarServices.stream()
        .filter(svc -> svc.getProvider().equalsIgnoreCase(user.getProvider()))
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "No calendar integration available for provider " + user.getProvider()));
  }

  public void disconnectCurrentUserCalendar() {
    getCurrentUserCalendar().disconnectUser();
  }
}

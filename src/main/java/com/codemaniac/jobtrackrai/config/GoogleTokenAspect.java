package com.codemaniac.jobtrackrai.config;

import com.codemaniac.jobtrackrai.entity.User;
import com.codemaniac.jobtrackrai.service.CurrentUserService;
import com.codemaniac.jobtrackrai.service.GoogleTokenRefreshService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleTokenAspect {

  private final CurrentUserService currentUserService;
  private final GoogleTokenRefreshService tokenRefreshService;

  @Around("@annotation(com.codemaniac.jobtrackrai.annotation.EnsureGoogleAccessTokenFresh)")
  public Object ensureFreshToken(final ProceedingJoinPoint pjp) throws Throwable {

    final User user = currentUserService.getCurrentUser();

    if (user.getGoogleAccessToken() != null) {
      log.debug("Checking Google token freshness for {}", user.getEmail());
      try {
        tokenRefreshService.refreshAccessToken(user);
      } catch (final Exception e) {
        log.error("Failed to refresh token for {}: {}", user.getEmail(), e.getMessage());
      }
    } else {
      log.warn("User {} has no Google access token", user.getEmail());
    }

    return pjp.proceed();
  }
}

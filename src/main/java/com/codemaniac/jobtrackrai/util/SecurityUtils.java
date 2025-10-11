package com.codemaniac.jobtrackrai.util;

import com.codemaniac.jobtrackrai.model.Audit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@Slf4j
public class SecurityUtils {

  private SecurityUtils() {}

  public static String getCurrentUsername() {

    try {
      final Object principal =
          SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      if (principal instanceof final UserDetails userDetails) {
        return userDetails.getUsername();
      }
      return Audit.SYSTEM;
    } catch (final Exception e) {
      log.warn("error while trying to get current userName", e);
      return null;
    }
  }
}

package com.codemaniac.jobtrackrai.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

public final class IndeedJobUrlValidator {

  private static final Set<String> ALLOWED_DOMAINS =
      Set.of(
          "indeed.com", "www.indeed.com", "indeed.co.uk", "www.indeed.co.uk", "ng.indeed.com"
          // add more locales as needed
          );

  private IndeedJobUrlValidator() {}

  public static boolean isValid(final String url) {
    try {
      final URI uri = new URI(url);

      if (!isHttp(uri)) {
        return false;
      }

      if (!isIndeedHost(uri.getHost())) {
        return false;
      }

      final String jobKey = extractJobKey(uri.getQuery());
      return jobKey != null && isValidJobKey(jobKey);

    } catch (final URISyntaxException e) {
      return false;
    }
  }

  private static boolean isHttp(final URI uri) {
    return "http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme());
  }

  private static boolean isIndeedHost(final String host) {
    return host != null && ALLOWED_DOMAINS.contains(host.toLowerCase());
  }

  private static String extractJobKey(final String query) {
    if (query == null) {
      return null;
    }

    for (final String param : query.split("&")) {
      if (param.startsWith("jk=")) {
        return param.substring(3);
      }
    }
    return null;
  }

  private static boolean isValidJobKey(final String jobKey) {
    // Indeed job keys are typically 16–18 hex-like chars
    return jobKey.matches("[a-zA-Z0-9]{10,20}");
  }
}

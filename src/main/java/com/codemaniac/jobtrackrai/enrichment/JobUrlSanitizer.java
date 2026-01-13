package com.codemaniac.jobtrackrai.enrichment;

import com.codemaniac.jobtrackrai.enums.JobSource;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class JobUrlSanitizer {

  private JobUrlSanitizer() {}

  public static Optional<String> sanitize(final String rawUrl) {
    if (rawUrl == null || rawUrl.isBlank()) {
      return Optional.empty();
    }

    final JobSource source = JobSourceDetector.fromUrl(rawUrl);

    return switch (source) {
      case INDEED -> sanitizeIndeed(rawUrl);
      case LINKEDIN -> Optional.of(rawUrl); // placeholder for future
      case GLASSDOOR -> Optional.of(rawUrl); // placeholder for future
      default -> Optional.of(rawUrl);
    };
  }

  /** Normalizes all Indeed URLs to: https://www.indeed.com/viewjob?jk={jobKey} */
  private static Optional<String> sanitizeIndeed(final String rawUrl) {
    try {
      final URI uri = URI.create(rawUrl);

      if (uri.getHost() == null || !uri.getHost().contains("indeed.com")) {
        return Optional.empty();
      }

      final String query = uri.getQuery();
      if (query == null) {
        return Optional.empty();
      }

      String jobKey = null;

      for (final String param : query.split("&")) {
        final String[] pair = param.split("=", 2);

        if (pair.length == 2) {
          final String key = pair[0];

          if ("jk".equals(key) || "vjk".equals(key)) {
            jobKey = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
            break;
          }
        }
      }

      if (jobKey == null || jobKey.isBlank()) {
        return Optional.empty();
      }

      return Optional.of("https://www.indeed.com/viewjob?jk=" + jobKey);

    } catch (final Exception e) {
      log.warn(e.getMessage());
      return Optional.empty();
    }
  }
}

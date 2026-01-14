package com.codemaniac.jobtrackrai.enums;

import java.util.Arrays;

public enum JobSource {
  INDEED("indeed.com"),
  LINKEDIN("linkedin.com"),
  GLASSDOOR("glassdoor.com"),
  MANUAL("manual"),
  UNKNOWN("unknown");

  private final String domain;

  JobSource(final String domain) {
    this.domain = domain;
  }

  public static JobSource fromInputUrl(final String url) {
    return Arrays.stream(values())
        .filter(s -> url.contains(s.domain))
        .findFirst()
        .orElseThrow(
            () -> new IllegalArgumentException("Unsupported job source for input URL: " + url));
  }
}

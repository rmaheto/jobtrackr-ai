package com.codemaniac.jobtrackrai.enrichment;

import com.codemaniac.jobtrackrai.enums.JobSource;

public final class JobSourceDetector {

  private JobSourceDetector() {}

  public static JobSource fromUrl(final String url) {
    if (url == null || url.isBlank()) {
      return JobSource.MANUAL;
    }

    final String lower = url.toLowerCase();

    if (lower.contains("indeed.com")) {
      return JobSource.INDEED;
    }

    if (lower.contains("linkedin.com/jobs")) {
      return JobSource.LINKEDIN;
    }

    if (lower.contains("glassdoor.com")) {
      return JobSource.GLASSDOOR;
    }

    return JobSource.UNKNOWN;
  }
}

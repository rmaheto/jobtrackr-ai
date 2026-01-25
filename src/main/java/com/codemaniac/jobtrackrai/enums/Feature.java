package com.codemaniac.jobtrackrai.enums;

import lombok.Getter;

@Getter
public enum Feature {
  LIMITED_APPLICATIONS("Limited Applications"),
  UNLIMITED_APPLICATIONS("Unlimited Applications"),
  ADVANCED_ANALYTICS("Advanced Analytics"),
  LIMITED_RESUMES("Limited Resumes"),
  UNLIMITED_RESUMES("Unlimited Resumes"),
  CALENDAR("Manage interviews and follow-ups"),
  PRIORITY_SUPPORT("Priority Support"),
  EXPORT_TO_EXCEL("Export Applications to Excel"),
  RUN_ENRICHMENT("Enrich Application Data");

  private final String displayName;

  Feature(final String displayName) {
    this.displayName = displayName;
  }
}

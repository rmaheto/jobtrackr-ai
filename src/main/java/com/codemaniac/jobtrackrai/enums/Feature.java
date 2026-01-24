package com.codemaniac.jobtrackrai.enums;

public enum Feature {
  LIMITED_APPLICATIONS("Limited Applications"),
  UNLIMITED_APPLICATIONS("Unlimited Applications"),
  ADVANCED_ANALYTICS("Advanced Analytics"),
  LIMITED_RESUMES("Limited Resumes"),
  UNLIMITED_RESUMES("Unlimited Resumes"),
  CALENDAR("Manage interviews and follow-ups"),
  PRIORITY_SUPPORT("Priority Support");

  private final String displayName;

  Feature(final String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}

package com.codemaniac.jobtrackrai.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LandingPage {
  DASHBOARD("Dashboard"),
  APPLICATIONS("Applications"),
  ANALYTICS("Analytics"),
  RESUMES("Resumes"),
  CALENDAR("Calendar"),
  PREFERENCES("preferences"),
  PROFILE("Profile");

  private final String label;
}

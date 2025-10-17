package com.codemaniac.jobtrackrai.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LandingPage {
  DASHBOARD("Dashboard"),
  APPLICATIONS("Applications"),
  FOLLOWUPS("Follow-ups"),
  SETTINGS("Settings");

  private final String label;
}

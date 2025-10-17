package com.codemaniac.jobtrackrai.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Theme {
  LIGHT("Light"),
  DARK("Dark"),
  SYSTEM("System");

  private final String label;
}

package com.codemaniac.jobtrackrai.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FollowUpReminder {
  ONE_DAY("1 day", 1),
  THREE_DAYS("3 days", 3),
  ONE_WEEK("1 week", 7),
  TWO_WEEKS("2 weeks", 14);

  private final String label;
  private final int days;
}

package com.codemaniac.jobtrackrai.dto;

import lombok.Data;

@Data
public class DateRepresentation {

  private String utc;
  private String local;
  private String formattedDateTime; // full date-time (e.g., Oct 17 2025 8:30 AM)
  private String formattedDate; // date-only (e.g., Oct 17 2025)
  private String relative; // e.g., "2 hours ago"

  public DateRepresentation(
      final String utc,
      final String local,
      final String formattedDateTime,
      final String formattedDate,
      final String relative) {
    this.utc = utc;
    this.local = local;
    this.formattedDateTime = formattedDateTime;
    this.formattedDate = formattedDate;
    this.relative = relative;
  }
}

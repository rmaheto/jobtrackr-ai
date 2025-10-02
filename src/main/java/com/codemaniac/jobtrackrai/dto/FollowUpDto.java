package com.codemaniac.jobtrackrai.dto;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FollowUpDto {
  private Long id;
  private OffsetDateTime scheduledAt;
  private String type;
  private String notes;
  private boolean completed;
  private String applicationId;
  private String company;
  private String applicationTitle;
}

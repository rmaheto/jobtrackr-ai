package com.codemaniac.jobtrackrai.dto;

import com.codemaniac.jobtrackrai.enums.FollowUpType;
import java.time.OffsetDateTime;
import lombok.Data;

@Data
public class FollowUpRequest {
  private OffsetDateTime scheduledAt;
  private FollowUpType type;
  private String notes;
}

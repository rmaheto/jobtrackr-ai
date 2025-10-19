package com.codemaniac.jobtrackrai.dto;

import com.codemaniac.jobtrackrai.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JobApplicationSummaryDto {
  private Long id;
  private String company;
  private String role;
  private Status status;
  private DateRepresentation appliedDate;
}

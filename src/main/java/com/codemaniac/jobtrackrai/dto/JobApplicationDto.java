package com.codemaniac.jobtrackrai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobApplicationDto {
  private Long id;
  private String company;
  private String role;
  private String location;
  private String jobType;
  private String skills;
  private String salary;
  private String jobLink;
  private String description;
  private String status;
  private DateRepresentation appliedDate;
  private String contactPersonName;
  private String contactPersonEmail;
  private String notes;
  private Long linkedResumeId;
}

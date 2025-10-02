package com.codemaniac.jobtrackrai.dto;

import java.time.LocalDate;
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
  private LocalDate appliedDate;
  private String contactPersonName;
  private String contactPersonEmail;
  private String notes;
}


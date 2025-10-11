package com.codemaniac.jobtrackrai.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobApplicationSearchRequest {
  private String searchTerm;
  private String company;
  private String role;
  private String location;
  private String jobType;
  private String skills;
  private String status;
  private String recordStatus;
  private LocalDate fromDate;
  private LocalDate toDate;
}


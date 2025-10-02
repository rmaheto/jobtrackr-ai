package com.codemaniac.jobtrackrai.dto;


import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class RecentApplicationDto {
  private Long id;
  private String company;
  private String role;
  private String status;
  private LocalDate appliedDate;
}


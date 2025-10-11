package com.codemaniac.jobtrackrai.dto;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecentApplicationDto {
  private Long id;
  private String company;
  private String role;
  private String status;
  private LocalDate appliedDate;
}

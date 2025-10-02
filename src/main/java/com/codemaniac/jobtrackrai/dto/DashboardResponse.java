package com.codemaniac.jobtrackrai.dto;


import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class DashboardResponse {
  private long totalApplications;
  private long applied;
  private long interviews;
  private long offers;
  private long rejected;
  private List<RecentApplicationDto> recentApplications;
}


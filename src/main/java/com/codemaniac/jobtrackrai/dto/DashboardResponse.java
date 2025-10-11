package com.codemaniac.jobtrackrai.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

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

package com.codemaniac.jobtrackrai.service;

import com.codemaniac.jobtrackrai.dto.DashboardResponse;
import com.codemaniac.jobtrackrai.dto.RecentApplicationDto;
import com.codemaniac.jobtrackrai.entity.JobApplication;
import com.codemaniac.jobtrackrai.enums.Status;
import com.codemaniac.jobtrackrai.model.Audit;
import com.codemaniac.jobtrackrai.repository.JobApplicationRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

  private final JobApplicationRepository repository;
  private final CurrentUserService currentUserService;

  public DashboardResponse getDashboardData() {
    final var user = currentUserService.getCurrentUser();
    final List<JobApplication> apps =
        repository.findByUserIdAndAudit_RecordStatusOrderByAudit_CreateTimestampDesc(
            user.getId(), Audit.RECORD_STATUS_ACTIVE);

    final long total = apps.size();
    final Map<Status, Long> counts =
        apps.stream()
            .collect(Collectors.groupingBy(JobApplication::getStatus, Collectors.counting()));
    final long applied = counts.getOrDefault(Status.APPLIED, 0L);
    final long interviews = counts.getOrDefault(Status.INTERVIEW, 0L);
    final long offers = counts.getOrDefault(Status.OFFER, 0L);
    final long rejected = counts.getOrDefault(Status.REJECTED, 0L);

    final List<RecentApplicationDto> recent =
        apps.stream()
            .limit(5)
            .map(
                a ->
                    RecentApplicationDto.builder()
                        .id(a.getId())
                        .company(a.getCompany())
                        .role(a.getRole())
                        .status(a.getStatus().name())
                        .appliedDate(a.getAppliedDate())
                        .build())
            .toList();

    return DashboardResponse.builder()
        .totalApplications(total)
        .applied(applied)
        .interviews(interviews)
        .offers(offers)
        .rejected(rejected)
        .recentApplications(recent)
        .build();
  }
}

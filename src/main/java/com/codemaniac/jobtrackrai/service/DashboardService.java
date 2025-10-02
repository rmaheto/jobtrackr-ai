package com.codemaniac.jobtrackrai.service;

import com.codemaniac.jobtrackrai.dto.DashboardResponse;
import com.codemaniac.jobtrackrai.dto.RecentApplicationDto;
import com.codemaniac.jobtrackrai.enums.Status;
import com.codemaniac.jobtrackrai.model.Audit;
import com.codemaniac.jobtrackrai.repository.JobApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

  private final JobApplicationRepository repository;
  private final CurrentUserService currentUserService;

  public DashboardResponse getDashboardData() {
    final var user = currentUserService.getCurrentUser();
    final var apps =
        repository.findLiteByUserIdAndStatus(user.getId(), Audit.RECORD_STATUS_ACTIVE);

    final long total = apps.size();
    final long applied = apps.stream().filter(a -> a.getStatus() == Status.APPLIED).count();
    final long interviews = apps.stream().filter(a -> a.getStatus() == Status.INTERVIEW).count();
    final long offers = apps.stream().filter(a -> a.getStatus() == Status.OFFER).count();
    final long rejected = apps.stream().filter(a -> a.getStatus() == Status.REJECTED).count();

    final List<RecentApplicationDto> recent =
        apps.stream()
            .sorted((a, b) -> b.getAppliedDate().compareTo(a.getAppliedDate()))
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

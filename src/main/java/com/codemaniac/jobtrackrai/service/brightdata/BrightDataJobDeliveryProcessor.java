package com.codemaniac.jobtrackrai.service.brightdata;

import com.codemaniac.jobtrackrai.dto.brightdata.IndeedJobSnapshotResponse;
import com.codemaniac.jobtrackrai.entity.JobApplication;
import com.codemaniac.jobtrackrai.enums.EnrichmentStatus;
import com.codemaniac.jobtrackrai.mapper.JobSnapshotToJobApplicationMapper;
import com.codemaniac.jobtrackrai.repository.JobApplicationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BrightDataJobDeliveryProcessor {

  private final JobApplicationRepository jobApplicationRepository;

  @Async
  public void handleDeliveredJobs(final List<IndeedJobSnapshotResponse> jobs) {

    for (final IndeedJobSnapshotResponse job : jobs) {

      final String jobUrl = job.getUrl();
      final String jobId = job.getJobId();

      final JobApplication application =
          jobApplicationRepository.findByJobLink(jobUrl).orElse(null);

      if (shouldSkip(application)) {
        if (application == null) {
          log.warn("No JobApplication found for Indeed jobId={}", jobId);
        } else {
          log.info("JobApplication id={} already enriched. Skipping.", application.getId());
        }
        continue;
      }

      try {
        JobSnapshotToJobApplicationMapper.merge(job, application);
        application.setEnrichmentStatus(EnrichmentStatus.ENRICHED);
      } catch (final Exception e) {
        application.setEnrichmentStatus(EnrichmentStatus.FAILED);
        log.error(
            "Failed to enrich JobApplication id={} from jobId={}", application.getId(), jobId, e);
      }

      jobApplicationRepository.save(application);
    }
  }

  private boolean shouldSkip(final JobApplication application) {
    return application == null || application.getEnrichmentStatus() == EnrichmentStatus.ENRICHED;
  }
}

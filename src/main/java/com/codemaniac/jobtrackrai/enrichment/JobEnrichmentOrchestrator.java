package com.codemaniac.jobtrackrai.enrichment;

import com.codemaniac.jobtrackrai.dto.JobApplicationRequest;
import com.codemaniac.jobtrackrai.enrichment.strategy.JobScrapingStrategyRegistry;
import com.codemaniac.jobtrackrai.entity.JobApplication;
import com.codemaniac.jobtrackrai.enums.EnrichmentStatus;
import com.codemaniac.jobtrackrai.enums.Feature;
import com.codemaniac.jobtrackrai.enums.JobSource;
import com.codemaniac.jobtrackrai.security.RequiresFeature;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JobEnrichmentOrchestrator {

  private final JobScrapingStrategyRegistry registry;

  @RequiresFeature({Feature.RUN_ENRICHMENT})
  public void maybeEnrich(final JobApplication job, final JobApplicationRequest request) {

    if (!request.getScrapeFromUrl().orElse(false)) {
      return;
    }

    final String sanitizedUrl = JobUrlSanitizer.sanitize(job.getJobLink()).orElse(job.getJobLink());

    job.setJobLink(sanitizedUrl);

    final JobSource source = request.getJobSource().orElse(JobSourceDetector.fromUrl(sanitizedUrl));

    final String snapshotId = registry.get(source).triggerScrape(sanitizedUrl, job.getId(), source);

    job.setSnapshotId(snapshotId);
    job.setEnrichmentStatus(EnrichmentStatus.PENDING_ENRICHMENT);
  }
}

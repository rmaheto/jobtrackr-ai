package com.codemaniac.jobtrackrai.enrichment;

import com.codemaniac.jobtrackrai.dto.JobApplicationRequest;
import com.codemaniac.jobtrackrai.enrichment.strategy.JobScrapingStrategyRegistry;
import com.codemaniac.jobtrackrai.entity.JobApplication;
import com.codemaniac.jobtrackrai.enums.EnrichmentStatus;
import com.codemaniac.jobtrackrai.enums.JobSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JobEnrichmentOrchestrator {

  private final JobScrapingStrategyRegistry registry;

  public void maybeEnrich(final JobApplication job, final JobApplicationRequest request) {

    if (!request.getScrapeFromUrl().orElse(false)) {
      return;
    }

    final String sanitizedUrl = JobUrlSanitizer.sanitize(job.getJobLink()).orElse(job.getJobLink());

    job.setJobLink(sanitizedUrl);

    final JobSource source = request.getJobSource().orElse(JobSourceDetector.fromUrl(sanitizedUrl));

    registry.get(source).triggerScrape(sanitizedUrl, job.getId());

    job.setEnrichmentStatus(EnrichmentStatus.PENDING_ENRICHMENT);
  }
}

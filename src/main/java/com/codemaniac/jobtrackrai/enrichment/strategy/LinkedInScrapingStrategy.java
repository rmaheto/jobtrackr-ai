package com.codemaniac.jobtrackrai.enrichment.strategy;

import com.codemaniac.jobtrackrai.enums.JobSource;
import com.codemaniac.jobtrackrai.service.brightdata.LinkedInSnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LinkedInScrapingStrategy implements JobScrapingStrategy {

  private final LinkedInSnapshotService linkedInSnapshotService;

  @Override
  public JobSource supports() {
    return JobSource.LINKEDIN;
  }

  @Override
  public String triggerScrape(
      final String jobUrl, final Long jobApplicationId, final JobSource jobSource) {
    return linkedInSnapshotService.requestSnapshot(jobUrl);
  }
}

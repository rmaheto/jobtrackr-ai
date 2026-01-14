package com.codemaniac.jobtrackrai.enrichment.strategy;

import com.codemaniac.jobtrackrai.enums.JobSource;
import com.codemaniac.jobtrackrai.service.brightdata.GlassdoorSnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GlassdoorScrapingStrategy implements JobScrapingStrategy {

  private final GlassdoorSnapshotService glassdoorSnapshotService;

  @Override
  public JobSource supports() {
    return JobSource.GLASSDOOR;
  }

  @Override
  public String triggerScrape(
      final String jobUrl, final Long jobApplicationId, final JobSource jobSource) {
    return glassdoorSnapshotService.requestSnapshot(jobUrl);
  }
}

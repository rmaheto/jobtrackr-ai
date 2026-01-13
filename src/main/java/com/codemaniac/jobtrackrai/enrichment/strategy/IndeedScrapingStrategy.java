package com.codemaniac.jobtrackrai.enrichment.strategy;

import com.codemaniac.jobtrackrai.enums.JobSource;
import com.codemaniac.jobtrackrai.service.brightdata.BrightDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IndeedScrapingStrategy implements JobScrapingStrategy {

  private final BrightDataService brightDataService;

  @Override
  public JobSource supports() {
    return JobSource.INDEED;
  }

  @Override
  public void triggerScrape(final String jobUrl, final Long jobApplicationId) {
    brightDataService.createSnapshot(jobUrl);
  }
}

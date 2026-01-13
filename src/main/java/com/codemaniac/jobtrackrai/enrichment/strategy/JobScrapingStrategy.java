package com.codemaniac.jobtrackrai.enrichment.strategy;

import com.codemaniac.jobtrackrai.enums.JobSource;

public interface JobScrapingStrategy {

  JobSource supports();

  void triggerScrape(String jobUrl, Long jobApplicationId);
}

package com.codemaniac.jobtrackrai.enrichment.strategy;

import com.codemaniac.jobtrackrai.enums.JobSource;

public interface JobScrapingStrategy {

  JobSource supports();

  String triggerScrape(String jobUrl, Long jobApplicationId, final JobSource jobSource);
}

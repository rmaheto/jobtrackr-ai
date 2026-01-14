package com.codemaniac.jobtrackrai.enrichment.strategy;

import com.codemaniac.jobtrackrai.entity.JobApplication;
import com.codemaniac.jobtrackrai.enums.JobSource;
import com.fasterxml.jackson.databind.JsonNode;

public interface JobScrapingStrategy {

  JobSource supports();

  String triggerScrape(String jobUrl, Long jobApplicationId, final JobSource jobSource);

  void handleDelivery(JsonNode payload, JobApplication jobApplication);
}

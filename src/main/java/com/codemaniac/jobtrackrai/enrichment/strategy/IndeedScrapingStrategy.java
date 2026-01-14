package com.codemaniac.jobtrackrai.enrichment.strategy;

import com.codemaniac.jobtrackrai.dto.brightdata.IndeedJobSnapshotResponse;
import com.codemaniac.jobtrackrai.entity.JobApplication;
import com.codemaniac.jobtrackrai.enums.JobSource;
import com.codemaniac.jobtrackrai.mapper.JobSnapshotToJobApplicationMapper;
import com.codemaniac.jobtrackrai.service.brightdata.IndeedSnapshotService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IndeedScrapingStrategy implements JobScrapingStrategy {

  private final IndeedSnapshotService indeedSnapshotService;
  private final ObjectMapper objectMapper;

  @Override
  public JobSource supports() {
    return JobSource.INDEED;
  }

  @Override
  public String triggerScrape(
      final String jobUrl, final Long jobApplicationId, final JobSource jobSource) {
    return indeedSnapshotService.requestSnapshot(jobUrl);
  }

  @Override
  public void handleDelivery(final JsonNode payload, final JobApplication application) {

    final List<IndeedJobSnapshotResponse> jobs =
        objectMapper.convertValue(payload, new TypeReference<>() {});

    JobSnapshotToJobApplicationMapper.merge(jobs.get(0), application);
  }
}

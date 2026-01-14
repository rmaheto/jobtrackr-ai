package com.codemaniac.jobtrackrai.service.brightdata;

import com.codemaniac.jobtrackrai.enrichment.strategy.JobScrapingStrategy;
import com.codemaniac.jobtrackrai.enrichment.strategy.JobScrapingStrategyRegistry;
import com.codemaniac.jobtrackrai.entity.JobApplication;
import com.codemaniac.jobtrackrai.enums.EnrichmentStatus;
import com.codemaniac.jobtrackrai.enums.JobSource;
import com.codemaniac.jobtrackrai.repository.JobApplicationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class BrightDataJobDeliveryProcessor {

  private final JobApplicationRepository jobApplicationRepository;

  private final JobScrapingStrategyRegistry strategyRegistry;

  @Async
  @Transactional
  public void process(final JsonNode payload) {

    final JsonNode first = validateAndGetFirst(payload);
    final String inputUrl = extractInputUrl(first);

    final JobApplication application = loadApplication(inputUrl);

    if (shouldSkip(application)) {
      log.info(
          "JobApplication id={} already processed. Status={}",
          application.getId(),
          application.getEnrichmentStatus());
      return;
    }

    enrich(application, payload);
  }

  private JsonNode validateAndGetFirst(final JsonNode payload) {
    if (!payload.isArray() || payload.isEmpty()) {
      log.debug("Received empty Bright Data webhook payload");
      throw new IllegalStateException("Empty Bright Data payload");
    }
    return payload.get(0);
  }

  private String extractInputUrl(final JsonNode first) {
    final String url = first.path("input").path("url").asText(null);

    if (url == null) {
      throw new IllegalStateException("Bright Data payload missing input.url");
    }
    return url;
  }

  private JobApplication loadApplication(final String inputUrl) {
    return jobApplicationRepository
        .findByJobLink(inputUrl)
        .orElseThrow(
            () -> new IllegalStateException("No JobApplication found for inputUrl=" + inputUrl));
  }

  private void enrich(final JobApplication application, final JsonNode payload) {

    final JobSource source = JobSource.fromInputUrl(application.getJobLink());
    final JobScrapingStrategy strategy = strategyRegistry.get(source);

    try {
      strategy.handleDelivery(payload, application);
      markEnriched(application);
    } catch (final Exception e) {
      markFailed(application, e);
    }
  }

  private void markEnriched(final JobApplication application) {
    application.setEnrichmentStatus(EnrichmentStatus.ENRICHED);
    jobApplicationRepository.save(application);
  }

  private void markFailed(final JobApplication application, final Exception e) {
    application.setEnrichmentStatus(EnrichmentStatus.FAILED);
    jobApplicationRepository.save(application);

    log.error(
        "Failed to enrich JobApplication id={} inputUrl={}",
        application.getId(),
        application.getJobLink(),
        e);
  }

  private boolean shouldSkip(final JobApplication application) {
    return application == null || application.getEnrichmentStatus() == EnrichmentStatus.ENRICHED;
  }
}

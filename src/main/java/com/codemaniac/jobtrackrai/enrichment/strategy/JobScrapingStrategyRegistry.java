package com.codemaniac.jobtrackrai.enrichment.strategy;

import com.codemaniac.jobtrackrai.enums.JobSource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class JobScrapingStrategyRegistry {

  private final Map<JobSource, JobScrapingStrategy> strategies;

  public JobScrapingStrategyRegistry(final List<JobScrapingStrategy> strategies) {
    this.strategies =
        strategies.stream()
            .collect(Collectors.toMap(JobScrapingStrategy::supports, Function.identity()));
  }

  public JobScrapingStrategy get(final JobSource source) {
    return Optional.ofNullable(strategies.get(source))
        .orElseThrow(() -> new IllegalArgumentException("No scraper for source: " + source));
  }
}

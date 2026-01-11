package com.codemaniac.jobtrackrai.config;

import java.net.SocketException;
import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

@Configuration
@EnableRetry
public class RetryConfig {

  private static final int MAX_RETRIES = 3;

  private static final int RETRY_FIXED_BACKOFF_MS = 500;

  @Bean
  public RetryTemplate retryTemplate() {
    return RetryTemplate.builder()
        .maxAttempts(MAX_RETRIES)
        .fixedBackoff(RETRY_FIXED_BACKOFF_MS)
        .retryOn(
            Arrays.asList(
                HttpServerErrorException.class,
                ResourceAccessException.class,
                SocketException.class))
        .build();
  }
}

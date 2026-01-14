package com.codemaniac.jobtrackrai.service.brightdata;

import com.codemaniac.jobtrackrai.dto.brightdata.BrightDataSnapshotRequest;
import com.codemaniac.jobtrackrai.dto.brightdata.BrightDataSnapshotResponse;
import com.codemaniac.jobtrackrai.dto.brightdata.IndeedJobSnapshotResponse;
import com.codemaniac.jobtrackrai.enums.BrightDataSource;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class BrightDataClient {

  @Value("${brightdata.base-url}")
  private String baseUrl;

  @Value("${brightdata.scrape-url}")
  private String scrapeUrl;

  @Value("${brightdata.download-snapshot-data-url}")
  private String downloadSnapshotDataUrl;

  @Value("${brightdata.webhook-callback-url}")
  private String webhookCallbackUrl;

  @Value("${brightdata.api-key}")
  private String apiKey;

  @Value("${brightdata.webhook-secret}")
  private String webhookSecret;

  private final RestTemplate restTemplate;

  private final RetryTemplate retryTemplate;

  public String requestSnapshotId(
      final String datasetId, final String jobUrl, final BrightDataSource source) {

    final String endpoint =
        baseUrl
            + scrapeUrl
            + "?dataset_id="
            + datasetId
            + "&endpoint="
            + webhookCallbackUrl
            + "&auth_header="
            + webhookSecret
            + "&notify=true&format=json&include_errors=true";

    final HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(apiKey);

    final BrightDataSnapshotRequest body =
        new BrightDataSnapshotRequest(List.of(new BrightDataSnapshotRequest.Input(jobUrl)));

    final HttpEntity<BrightDataSnapshotRequest> request = new HttpEntity<>(body, headers);

    return retryTemplate.execute(
        context -> {
          log.debug(
              "Requesting {} snapshot (attempt #{}) for url={}",
              source,
              context.getRetryCount() + 1,
              jobUrl);

          final BrightDataSnapshotResponse response =
              restTemplate.postForObject(endpoint, request, BrightDataSnapshotResponse.class);

          if (response == null || response.getSnapshotId() == null) {
            throw new IllegalStateException("Bright Data returned null or missing snapshot_id");
          }

          return response.getSnapshotId();
        },
        context -> {
          log.error(
              "{} snapshot request failed after {} attempts for url={}",
              source,
              context.getRetryCount(),
              jobUrl);

          throw new IllegalStateException(
              "Failed to obtain snapshot_id from Bright Data after retries");
        });
  }

  public List<IndeedJobSnapshotResponse> fetchSnapshotData(final String snapshotId) {

    final String endpoint = baseUrl + downloadSnapshotDataUrl + snapshotId + "?format=json";

    final HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(apiKey);

    final HttpEntity<Void> request = new HttpEntity<>(headers);

    return retryTemplate.execute(
        context -> {
          log.debug(
              "Downloading Bright Data snapshot (attempt #{}) snapshotId={}",
              context.getRetryCount() + 1,
              snapshotId);

          final ResponseEntity<List<IndeedJobSnapshotResponse>> response =
              restTemplate.exchange(
                  endpoint, HttpMethod.GET, request, new ParameterizedTypeReference<>() {});

          final List<IndeedJobSnapshotResponse> jobs = response.getBody();

          if (jobs == null || jobs.isEmpty()) {
            throw new IllegalStateException(
                "Snapshot data empty or unavailable for snapshotId=" + snapshotId);
          }

          return jobs;
        },
        context -> {
          log.error(
              "Failed to download snapshot data after {} attempts snapshotId={}",
              context.getRetryCount(),
              snapshotId);

          throw new IllegalStateException(
              "Unable to fetch snapshot data for snapshotId=" + snapshotId);
        });
  }
}

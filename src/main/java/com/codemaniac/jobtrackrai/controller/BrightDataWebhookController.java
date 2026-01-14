package com.codemaniac.jobtrackrai.controller;

import com.codemaniac.jobtrackrai.service.brightdata.BrightDataJobDeliveryProcessor;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/webhooks/brightdata")
public class BrightDataWebhookController {

  @Value("${brightdata.webhook-secret}")
  private String expectedWebhookSecret;

  private final BrightDataJobDeliveryProcessor jobDeliveryProcessor;

  /** Called by Bright Data when snapshot is ready */
  @PostMapping
  public ResponseEntity<Void> handleSnapshotReady(
      @RequestBody final JsonNode payload,
      @RequestHeader(value = "Authorization", required = false) final String authorization) {

    if (!expectedWebhookSecret.equals(authorization)) {
      log.warn("Rejected Bright Data webhook: invalid Authorization header");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    if (payload == null || payload.isEmpty()) {
      log.info("Received empty Bright Data webhook payload");
      return ResponseEntity.ok().build();
    }

    log.info("Received Bright Data delivery webhook with {} job(s)", payload.size());

    jobDeliveryProcessor.process(payload);

    return ResponseEntity.ok().build();
  }
}

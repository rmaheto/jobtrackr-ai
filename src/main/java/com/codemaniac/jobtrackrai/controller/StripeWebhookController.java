package com.codemaniac.jobtrackrai.controller;

import com.codemaniac.jobtrackrai.service.billing.StripeWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhooks/stripe")
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookController {

  private final StripeWebhookService webhookService;

  @PostMapping
  public ResponseEntity<Void> handleWebhook(
      @RequestBody final String payload,
      @RequestHeader("Stripe-Signature") final String signature) {
    webhookService.process(payload, signature);
    return ResponseEntity.ok().build();
  }
}

package com.codemaniac.jobtrackrai.controller;

import com.stripe.model.Event;
import com.stripe.model.Subscription;
import com.stripe.net.Webhook;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/payments")
public class WebhookController {

  @Value("${stripe.webhook-secret}")
  private String webhookSecret;

  @PostMapping("/webhook")
  public String handleWebhook(final HttpServletRequest request, @RequestBody final String payload)
      throws IOException {
    final String sigHeader = request.getHeader("Stripe-Signature");

    final Event event;
    try {
      event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
    } catch (final Exception e) {
      log.error("Webhook verification failed", e);
      return "";
    }

    switch (event.getType()) {
      case "customer.subscription.created":
      case "customer.subscription.updated":
      case "invoice.payment_succeeded":
        Subscription sub =
            (Subscription) event.getDataObjectDeserializer().getObject().orElse(null);
        if (sub != null) {
          log.info("Subscription {} status={}", sub.getId(), sub.getStatus());
          // TODO: update your DB (set user plan, billing date, etc.)
        }
        break;
      case "customer.subscription.deleted":
        log.info("Subscription canceled");
        // TODO: mark user plan inactive
        break;
      default:
        log.info("Unhandled event: {}", event.getType());
    }

    return "ok";
  }
}

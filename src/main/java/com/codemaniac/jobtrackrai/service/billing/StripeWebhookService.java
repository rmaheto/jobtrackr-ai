package com.codemaniac.jobtrackrai.service.billing;

import com.codemaniac.jobtrackrai.entity.PaymentEvent;
import com.codemaniac.jobtrackrai.repository.PaymentEventRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.Event;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookService {

  private final SubscriptionService subscriptionService;
  private final PaymentEventRepository paymentEventRepository;
  private final StripeWebhookVerifier stripeWebhookVerifier;
  private final ObjectMapper objectMapper;

  @Transactional
  public void process(final String payload, final String signature) {

    final Event event = stripeWebhookVerifier.verify(payload, signature);

    if (paymentEventRepository.existsByProviderEventId(event.getId())) {
      log.info("Skipping already processed event {}", event.getId());
      return;
    }

    final JsonNode payloadJson = parsePayload(payload);

    paymentEventRepository.save(
        PaymentEvent.builder()
            .providerEventId(event.getId())
            .eventType(event.getType())
            .payload(payloadJson)
            .processedAt(Instant.now())
            .build());

    dispatchEvent(event);
  }

  private void dispatchEvent(final Event event) {
    switch (event.getType()) {
      case "customer.subscription.created", "customer.subscription.updated" ->
          subscriptionService.syncFromProvider(event);

      case "customer.subscription.deleted" -> subscriptionService.cancelFromProvider(event);

      case "price.created", "price.updated" -> subscriptionService.syncPrice(event);

      default -> log.debug("Unhandled Stripe event {}", event.getType());
    }
  }

  private JsonNode parsePayload(final String payload) {
    try {
      return objectMapper.readTree(payload);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid JSON payload", e);
    }
  }
}

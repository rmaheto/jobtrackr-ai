package com.codemaniac.jobtrackrai.service.billing;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.codemaniac.jobtrackrai.entity.PaymentEvent;
import com.codemaniac.jobtrackrai.repository.PaymentEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.Event;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StripeWebhookServiceTest {

  private static final String PAYLOAD = "{ \"id\": \"evt_123\" }";
  private static final String SIGNATURE = "stripe-signature";

  @Mock private SubscriptionService subscriptionService;
  @Mock private PaymentEventRepository paymentEventRepository;
  @Mock private StripeWebhookVerifier stripeWebhookVerifier;
  @Mock private ObjectMapper objectMapper;
  @Mock private JsonNode payloadJson;
  @Mock private Event event;

  @InjectMocks private StripeWebhookService service;

  @Test
  void process_whenEventAlreadyProcessed_skipsHandling() {

    when(event.getId()).thenReturn("evt_123");
    when(paymentEventRepository.existsByProviderEventId("evt_123")).thenReturn(true);
    when(stripeWebhookVerifier.verify(PAYLOAD, SIGNATURE)).thenReturn(event);

    service.process(PAYLOAD, SIGNATURE);

    verifyNoInteractions(subscriptionService);
    verify(paymentEventRepository, never()).save(any(PaymentEvent.class));
  }

  @Test
  void process_whenSubscriptionCreated_callsSync() throws JsonProcessingException {

    when(event.getId()).thenReturn("evt_123");
    when(event.getType()).thenReturn("customer.subscription.created");
    when(paymentEventRepository.existsByProviderEventId("evt_123")).thenReturn(false);
    when(objectMapper.readTree(PAYLOAD)).thenReturn(payloadJson);
    when(stripeWebhookVerifier.verify(PAYLOAD, SIGNATURE)).thenReturn(event);

    service.process(PAYLOAD, SIGNATURE);

    verify(subscriptionService).syncFromProvider(event);
    verify(paymentEventRepository).save(any(PaymentEvent.class));
  }

  @Test
  void process_whenSubscriptionUpdated_callsSync() throws JsonProcessingException {

    when(event.getId()).thenReturn("evt_123");
    when(event.getType()).thenReturn("customer.subscription.updated");
    when(paymentEventRepository.existsByProviderEventId("evt_123")).thenReturn(false);
    when(objectMapper.readTree(PAYLOAD)).thenReturn(payloadJson);
    when(stripeWebhookVerifier.verify(PAYLOAD, SIGNATURE)).thenReturn(event);

    service.process(PAYLOAD, SIGNATURE);

    verify(subscriptionService).syncFromProvider(event);
  }

  @Test
  void process_whenSubscriptionDeleted_callsCancel() throws JsonProcessingException {

    when(event.getId()).thenReturn("evt_123");
    when(objectMapper.readTree(PAYLOAD)).thenReturn(payloadJson);
    when(event.getType()).thenReturn("customer.subscription.deleted");
    when(paymentEventRepository.existsByProviderEventId("evt_123")).thenReturn(false);
    when(stripeWebhookVerifier.verify(PAYLOAD, SIGNATURE)).thenReturn(event);

    service.process(PAYLOAD, SIGNATURE);

    verify(subscriptionService).cancelFromProvider(event);
  }

  @Test
  void process_whenUnhandledEventType_doesNothing() throws JsonProcessingException {

    when(objectMapper.readTree(PAYLOAD)).thenReturn(payloadJson);
    when(event.getId()).thenReturn("evt_123");
    when(event.getType()).thenReturn("invoice.created");
    when(paymentEventRepository.existsByProviderEventId("evt_123")).thenReturn(false);
    when(stripeWebhookVerifier.verify(PAYLOAD, SIGNATURE)).thenReturn(event);

    service.process(PAYLOAD, SIGNATURE);

    verifyNoInteractions(subscriptionService);
    verify(paymentEventRepository).save(any(PaymentEvent.class));
  }

  @Test
  void process_whenSignatureInvalid_throwsException() {

    when(stripeWebhookVerifier.verify(PAYLOAD, SIGNATURE))
        .thenThrow(new IllegalArgumentException("Invalid Stripe webhook signature"));

    assertThrows(IllegalArgumentException.class, () -> service.process(PAYLOAD, SIGNATURE));

    verifyNoInteractions(subscriptionService);
    verifyNoInteractions(paymentEventRepository);
  }
}

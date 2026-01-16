package com.codemaniac.jobtrackrai.service.billing;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class StripeWebhookVerifierTest {

  private static final String PAYLOAD = "{ \"id\": \"evt_123\" }";
  private static final String SIGNATURE = "stripe-signature";
  private static final String SECRET = "whsec_test";

  @Test
  void verify_whenSignatureValid_returnsEvent() {

    final StripeWebhookVerifier verifier = new StripeWebhookVerifier(SECRET);
    final Event expected = new Event();

    try (final MockedStatic<Webhook> webhook = mockStatic(Webhook.class)) {
      webhook.when(() -> Webhook.constructEvent(PAYLOAD, SIGNATURE, SECRET)).thenReturn(expected);

      final Event actual = verifier.verify(PAYLOAD, SIGNATURE);

      assertSame(expected, actual);
    }
  }

  @Test
  void verify_whenSignatureInvalid_throwsIllegalArgumentException() {

    final StripeWebhookVerifier verifier = new StripeWebhookVerifier(SECRET);

    try (final MockedStatic<Webhook> webhook = mockStatic(Webhook.class)) {
      webhook
          .when(() -> Webhook.constructEvent(PAYLOAD, SIGNATURE, SECRET))
          .thenThrow(new SignatureVerificationException("Invalid signature", SIGNATURE));

      final IllegalArgumentException ex =
          assertThrows(IllegalArgumentException.class, () -> verifier.verify(PAYLOAD, SIGNATURE));

      assertEquals("Invalid Stripe webhook signature", ex.getMessage());
    }
  }
}

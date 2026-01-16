package com.codemaniac.jobtrackrai.service.billing;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StripeWebhookVerifier {

  private final String webhookSecret;

  public StripeWebhookVerifier(@Value("${stripe.webhook.secret}") final String webhookSecret) {
    this.webhookSecret = webhookSecret;
  }

  public Event verify(final String payload, final String signature) {
    try {
      return Webhook.constructEvent(payload, signature, webhookSecret);
    } catch (final SignatureVerificationException e) {
      throw new IllegalArgumentException("Invalid Stripe webhook signature", e);
    }
  }
}

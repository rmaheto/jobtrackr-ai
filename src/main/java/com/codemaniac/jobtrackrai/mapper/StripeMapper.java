package com.codemaniac.jobtrackrai.mapper;

import com.codemaniac.jobtrackrai.dto.brightdata.ProviderSubscription;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.Subscription;
import java.time.Instant;

public final class StripeMapper {

  private StripeMapper() {}

  public static ProviderSubscription toSubscription(final Event event) {

    final var deserializer = event.getDataObjectDeserializer();
    if (deserializer == null) {
      throw new IllegalStateException("Stripe event missing data deserializer");
    }

    final StripeObject stripeObject =
        deserializer
            .getObject()
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Unable to deserialize Stripe event data object. "
                            + "Check api_version compatibility."));

    if (!(stripeObject instanceof final Subscription subscription)) {
      throw new IllegalArgumentException(
          "Event does not contain a Subscription: " + event.getType());
    }

    return new ProviderSubscription(
        subscription.getId(),
        subscription.getCustomer(),
        subscription.getItems().getData().get(0).getPrice().getId(),
        subscription.getStatus(),
        subscription.getStartDate() != null
            ? Instant.ofEpochSecond(subscription.getEndedAt())
            : null,
        subscription.getEndedAt() != null ? Instant.ofEpochSecond(subscription.getEndedAt()) : null,
        subscription.getCancelAtPeriodEnd());
  }
}

package com.codemaniac.jobtrackrai.mapper;

import com.codemaniac.jobtrackrai.dto.brightdata.ProviderSubscription;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.Subscription;
import java.time.Instant;

public final class StripeMapper {

  private StripeMapper() {}

  public static ProviderSubscription toSubscription(final Event event) {

    final StripeObject stripeObject = extractStripeObject(event);

    if (!(stripeObject instanceof final Subscription subscription)) {
      throw new IllegalArgumentException(
          "Event does not contain a Subscription: " + event.getType());
    }
    final String subscriptionId = subscription.getId();
    final String customerId = subscription.getCustomer();
    final String priceId = extractPriceId(subscription);

    final String status = subscription.getStatus();

    final Instant currentPeriodStart = toInstant(subscription.getStartDate());
    final Instant currentPeriodEnd = toInstant(subscription.getCancelAt());

    final boolean cancelAtPeriodEnd = Boolean.TRUE.equals(subscription.getCancelAtPeriodEnd());

    return new ProviderSubscription(
        subscriptionId,
        customerId,
        priceId,
        status,
        currentPeriodStart,
        currentPeriodEnd,
        cancelAtPeriodEnd);
  }

  private static StripeObject extractStripeObject(final Event event) {

    final var deserializer = event.getDataObjectDeserializer();
    if (deserializer == null) {
      throw new IllegalStateException("Stripe event missing data deserializer");
    }

    return deserializer
        .getObject()
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "Unable to deserialize Stripe event data object. "
                        + "Check api_version compatibility."));
  }

  private static Instant toInstant(final Long epochSeconds) {
    return epochSeconds == null ? null : Instant.ofEpochSecond(epochSeconds);
  }

  private static String extractPriceId(final Subscription subscription) {
    final var items = subscription.getItems().getData();
    if (items.isEmpty()) {
      throw new IllegalStateException("Stripe subscription has no items: " + subscription.getId());
    }
    return items.get(0).getPrice().getId();
  }
}

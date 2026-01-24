package com.codemaniac.jobtrackrai.mapper;

import com.codemaniac.jobtrackrai.entity.Plan;
import com.stripe.model.Event;
import com.stripe.model.Price;
import com.stripe.model.Price.Recurring;
import com.stripe.model.StripeObject;

public class StripePriceMapper {

  private StripePriceMapper() {}

  public static Plan toPlan(final Price price) {

    final String code = resolvePlanCode(price);

    final Recurring recurring = price.getRecurring();
    if (recurring == null) {
      throw new IllegalStateException("Price is not recurring: " + price.getId());
    }

    return Plan.builder()
        .code(code)
        .stripePriceId(price.getId())
        .stripeProductId(price.getProduct())
        .priceAmount(price.getUnitAmount()) // cents, DO NOT divide
        .currency(price.getCurrency().toUpperCase())
        .billingInterval(recurring.getInterval().toUpperCase())
        .intervalCount(recurring.getIntervalCount())
        .active(price.getActive())
        .build();
  }

  private static String resolvePlanCode(final Price price) {
    if (price.getLookupKey() != null && !price.getLookupKey().isBlank()) {
      return price.getLookupKey().toUpperCase();
    }
    return "STRIPE_" + price.getId();
  }

  private StripeObject extractStripeObject(final Event event) {
    final var deserializer = event.getDataObjectDeserializer();

    if (deserializer == null) {
      throw new IllegalStateException("Stripe event missing data object deserializer");
    }

    return deserializer
        .getObject()
        .orElseThrow(() -> new IllegalStateException("Unable to deserialize Stripe event object"));
  }
}

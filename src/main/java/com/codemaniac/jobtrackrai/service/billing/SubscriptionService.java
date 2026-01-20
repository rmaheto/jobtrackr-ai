package com.codemaniac.jobtrackrai.service.billing;

import com.codemaniac.jobtrackrai.dto.brightdata.ProviderSubscription;
import com.codemaniac.jobtrackrai.entity.Plan;
import com.codemaniac.jobtrackrai.entity.Subscription;
import com.codemaniac.jobtrackrai.entity.User;
import com.codemaniac.jobtrackrai.enums.SubscriptionStatus;
import com.codemaniac.jobtrackrai.exception.BillingException;
import com.codemaniac.jobtrackrai.mapper.StripeMapper;
import com.codemaniac.jobtrackrai.mapper.StripePriceMapper;
import com.codemaniac.jobtrackrai.repository.PlanRepository;
import com.codemaniac.jobtrackrai.repository.SubscriptionRepository;
import com.codemaniac.jobtrackrai.repository.UserRepository;
import com.stripe.model.Event;
import com.stripe.model.Price;
import com.stripe.model.StripeObject;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

  @Value("${billing.product-name}")
  private String productName;

  private final SubscriptionRepository subscriptionRepository;
  private final PlanRepository planRepository;
  private final UserRepository userRepository;

  @Transactional
  public void syncFromProvider(final Event event) {

    final ProviderSubscription dto = StripeMapper.toSubscription(event);

    final Subscription subscription =
        subscriptionRepository
            .findByProviderSubscriptionId(dto.subscriptionId())
            .orElseGet(() -> createNewSubscription(dto));

    updateSubscription(subscription, dto);

    subscriptionRepository.save(subscription);
  }

  @Transactional
  public void syncPrice(final Event event) {

    final StripeObject stripeObject = extractStripeObject(event);

    if (!(stripeObject instanceof final Price price)) {
      throw new IllegalArgumentException("Event does not contain a Price");
    }

    final Plan incoming = StripePriceMapper.toPlan(price);

    incoming.setName(buildPlanDisplayName(incoming.getBillingInterval()));

    final Plan plan = planRepository.findByStripePriceId(price.getId()).orElseGet(Plan::new);

    plan.mergeFrom(incoming);

    planRepository.save(plan);
  }

  @Transactional
  public void cancelFromProvider(final Event event) {

    final ProviderSubscription dto = StripeMapper.toSubscription(event);

    final Subscription subscription =
        subscriptionRepository
            .findByProviderSubscriptionId(dto.subscriptionId())
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Subscription not found for providerSubscriptionId="
                            + dto.subscriptionId()));

    subscription.setStatus(SubscriptionStatus.CANCELED);

    if (dto.currentPeriodEnd() != null) {
      subscription.setCurrentPeriodEnd(dto.currentPeriodEnd());
    }

    subscriptionRepository.save(subscription);
  }

  private Subscription createNewSubscription(final ProviderSubscription dto) {

    final User user =
        userRepository
            .findByStripeCustomerId(dto.customerId())
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "No user found for Stripe customerId=" + dto.customerId()));

    final Plan plan =
        planRepository
            .findActiveByStripePriceId(dto.priceId())
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "No plan mapped for Stripe priceId=" + dto.priceId()));

    return Subscription.builder()
        .user(user)
        .plan(plan)
        .providerCustomerId(dto.customerId())
        .providerSubscriptionId(dto.subscriptionId())
        .status(SubscriptionStatus.TRIALING)
        .currentPeriodStart(dto.currentPeriodStart())
        .currentPeriodEnd(dto.currentPeriodEnd())
        .build();
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

  private void updateSubscription(final Subscription subscription, final ProviderSubscription dto) {

    subscription.setStatus(mapStatus(dto.status()));

    if (dto.currentPeriodStart() != null) {
      subscription.setCurrentPeriodStart(dto.currentPeriodStart());
    }

    if (dto.currentPeriodEnd() != null) {
      subscription.setCurrentPeriodEnd(dto.currentPeriodEnd());
    }

    if (dto.cancelAtPeriodEnd() != null) {
      subscription.setCancelAtPeriodEnd(dto.cancelAtPeriodEnd());
    }

    if (hasPriceChanged(subscription, dto)) {
      applyPlanChange(subscription, dto);
    }
  }

  private void applyPlanChange(final Subscription subscription, final ProviderSubscription dto) {
    final Plan plan = resolvePlanByPriceId(dto.priceId());

    subscription.setPlan(plan);
  }

  private boolean hasPriceChanged(final Subscription subscription, final ProviderSubscription dto) {
    return !Objects.equals(subscription.getPlan().getStripePriceId(), dto.priceId());
  }

  private Plan resolvePlanByPriceId(final String stripePriceId) {
    return planRepository
        .findActiveByStripePriceId(stripePriceId)
        .orElseThrow(
            () -> new BillingException("No plan found for Stripe price: " + stripePriceId));
  }

  private SubscriptionStatus mapStatus(final String providerStatus) {

    return switch (providerStatus) {
      case "trialing" -> SubscriptionStatus.TRIALING;

      case "active" -> SubscriptionStatus.ACTIVE;

      case "past_due", "unpaid" -> SubscriptionStatus.PAST_DUE;

      case "canceled" -> SubscriptionStatus.CANCELED;

      case "incomplete", "incomplete_expired" -> SubscriptionStatus.EXPIRED;

      default ->
          throw new IllegalArgumentException(
              "Unknown provider subscription status: " + providerStatus);
    };
  }

  private String buildPlanDisplayName(final String billingInterval) {

    if (billingInterval == null || billingInterval.isBlank()) {
      return productName;
    }

    final String displayInterval =
        switch (billingInterval.toLowerCase()) {
          case "month" -> "Monthly";
          case "year" -> "Yearly";
          default -> billingInterval;
        };

    return "%s %s".formatted(productName, displayInterval);
  }
}

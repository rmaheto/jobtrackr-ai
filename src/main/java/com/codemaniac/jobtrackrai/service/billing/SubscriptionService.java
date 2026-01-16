package com.codemaniac.jobtrackrai.service.billing;

import com.codemaniac.jobtrackrai.dto.brightdata.ProviderSubscription;
import com.codemaniac.jobtrackrai.entity.Plan;
import com.codemaniac.jobtrackrai.entity.Subscription;
import com.codemaniac.jobtrackrai.entity.User;
import com.codemaniac.jobtrackrai.enums.SubscriptionStatus;
import com.codemaniac.jobtrackrai.mapper.StripeMapper;
import com.codemaniac.jobtrackrai.repository.PlanRepository;
import com.codemaniac.jobtrackrai.repository.SubscriptionRepository;
import com.codemaniac.jobtrackrai.repository.UserRepository;
import com.stripe.model.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

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
            .findByStripePriceId(dto.priceId())
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
}

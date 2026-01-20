package com.codemaniac.jobtrackrai.service.billing;

import com.codemaniac.jobtrackrai.dto.billing.BillingSummaryResponse;
import com.codemaniac.jobtrackrai.dto.billing.InvoiceSummary;
import com.codemaniac.jobtrackrai.dto.billing.PaymentMethodSummary;
import com.codemaniac.jobtrackrai.dto.billing.PlanResponse;
import com.codemaniac.jobtrackrai.dto.billing.StripePaymentMethodService;
import com.codemaniac.jobtrackrai.entity.Plan;
import com.codemaniac.jobtrackrai.entity.Subscription;
import com.codemaniac.jobtrackrai.entity.User;
import com.codemaniac.jobtrackrai.enums.SubscriptionStatus;
import com.codemaniac.jobtrackrai.exception.BillingException;
import com.codemaniac.jobtrackrai.repository.PlanRepository;
import com.codemaniac.jobtrackrai.repository.SubscriptionRepository;
import com.codemaniac.jobtrackrai.repository.UserRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.Nonnull;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingService {

  @Value("${stripe.checkout.success-url}")
  private String successUrl;

  @Value("${stripe.checkout.cancel-url}")
  private String cancelUrl;

  @Value("${app.frontend.url}")
  private String appUrl;

  private final PlanRepository planRepository;
  private final UserRepository userRepository;
  private final SubscriptionRepository subscriptionRepository;
  private final StripePaymentMethodService stripePaymentMethodService;
  private final StripeInvoiceService stripeInvoiceService;

  @Transactional(readOnly = true)
  public List<PlanResponse> getAvailablePlans() {

    return planRepository.findAllByActiveTrue().stream()
        .map(
            plan ->
                PlanResponse.builder()
                    .code(plan.getCode())
                    .name(plan.getName())
                    .priceAmount(plan.getPriceAmount())
                    .currency(plan.getCurrency())
                    .billingInterval(plan.getBillingInterval().toLowerCase())
                    .features(resolveFeatures(plan.getCode()))
                    .build())
        .toList();
  }

  public String createBillingPortalUrl(@Nonnull final User user) {

    if (user.getStripeCustomerId() == null) {
      throw new BillingException("User has no billing account");
    }

    try {
      final com.stripe.param.billingportal.SessionCreateParams params =
          com.stripe.param.billingportal.SessionCreateParams.builder()
              .setCustomer(user.getStripeCustomerId())
              .setReturnUrl(appUrl + "/account?portal=return")
              .build();

      final com.stripe.model.billingportal.Session session =
          com.stripe.model.billingportal.Session.create(params);
      return session.getUrl();

    } catch (final StripeException e) {
      log.warn("Failed to create billing portal url", e);
      throw new BillingException("Failed to open billing portal. Please try again.", e);
    }
  }

  @Transactional
  public String createCheckoutSession(final User user, final String planCode) {

    subscriptionRepository
        .findActiveByUserId(user.getId(), Instant.now())
        .ifPresent(
            s -> {
              throw new BillingException(
                  "User already has an active subscription. Use billing portal instead.");
            });

    if ("free".equalsIgnoreCase(planCode)) {
      throw new BillingException("FREE plan cannot be purchased");
    }

    final Plan plan =
        planRepository
            .findByCodeAndActiveTrue(planCode)
            .orElseThrow(() -> new BillingException("Plan not found or inactive: " + planCode));

    if (plan.getStripePriceId() == null) {
      log.warn("Plan: {} has no stripe price id", planCode);
      throw new BillingException("Plan is missing Stripe price id: " + planCode);
    }

    try {
      if (user.getStripeCustomerId() == null) {
        final Customer customer = Customer.create(Map.of("email", user.getEmail()));

        user.setStripeCustomerId(customer.getId());
        userRepository.save(user);
      }

      final SessionCreateParams params =
          SessionCreateParams.builder()
              .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
              .setCustomer(user.getStripeCustomerId())
              .setSuccessUrl(successUrl)
              .setCancelUrl(cancelUrl)
              .addLineItem(
                  SessionCreateParams.LineItem.builder()
                      .setPrice(plan.getStripePriceId())
                      .setQuantity(1L)
                      .build())
              .putMetadata("userId", user.getId().toString())
              .putMetadata("planCode", planCode)
              .build();

      final Session session = Session.create(params);
      return session.getUrl();

    } catch (final StripeException e) {
      log.warn(
          "Failed to create checkout session for user: {} with Plan Code: {}", user, planCode, e);
      throw new BillingException("Failed to create Stripe checkout session", e);
    }
  }

  @Transactional
  public void requestCancellation(final Long userId) {

    final Subscription subscription =
        subscriptionRepository
            .findActiveByUserId(userId, Instant.now())
            .orElseThrow(() -> new BillingException("No active subscription to cancel"));

    try {
      final com.stripe.model.Subscription stripeSub =
          com.stripe.model.Subscription.retrieve(subscription.getProviderSubscriptionId());

      stripeSub.update(Map.of("cancel_at_period_end", true));

    } catch (final StripeException e) {
      log.warn(
          "Failed to request cancellation for userId:{} with subscription: {}",
          userId,
          subscription,
          e);
      throw new BillingException("Failed to cancel subscription. Please try again.", e);
    }
  }

  public BillingSummaryResponse getSummary(final Long userId) {

    final Subscription subscription =
        subscriptionRepository.findActiveByUserId(userId, Instant.now()).orElse(null);

    if (subscription == null) {
      return BillingSummaryResponse.builder()
          .planName("Free")
          .planCode("FREE")
          .status("FREE")
          .canUpgrade(true)
          .canCancel(false)
          .build();
    }

    PaymentMethodSummary paymentMethodSummary = null;
    try {
      paymentMethodSummary =
          stripePaymentMethodService.getDefaultPaymentMethod(subscription.getProviderCustomerId());
    } catch (final Exception e) {
      log.warn("Failed to get payment method summary for user: {}", userId, e);
    }

    final Plan plan = subscription.getPlan();

    return BillingSummaryResponse.builder()
        .planName(plan.getName())
        .planCode(plan.getCode())
        .priceAmount(plan.getPriceAmount())
        .currency(plan.getCurrency())
        .billingInterval(plan.getBillingInterval())
        .status(subscription.getStatus().name().toLowerCase())
        .currentPeriodEnd(subscription.getCurrentPeriodEnd())
        .nextBillingDate(subscription.getCurrentPeriodEnd())
        .features(resolveFeatures(plan.getCode()))
        .paymentMethod(paymentMethodSummary)
        .canCancel(subscription.getStatus() == SubscriptionStatus.ACTIVE)
        .canUpgrade(true)
        .build();
  }

  public List<InvoiceSummary> getInvoiceHistory(final Long userId) {

    final User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalStateException("User not found"));

    if (user.getStripeCustomerId() == null) {
      return List.of();
    }

    try {
      return stripeInvoiceService.getRecentInvoices(user.getStripeCustomerId(), 10);
    } catch (final StripeException e) {
      log.warn("Failed to get invoice history for user: {}", userId, e);
      return List.of();
    }
  }

  private List<String> resolveFeatures(final String code) {
    return switch (code) {
      case "FREE" -> List.of("Limited Applications");
      case "PRO_MONTHLY", "PRO_YEARLY" ->
          List.of(
              "Unlimited Applications",
              "Advanced Analytics",
              "Resume Builder",
              "Interview Scheduler");
      case "TEAM_MONTHLY", "TEAM_YEARLY" ->
          List.of("Everything in Pro", "Team Management", "Custom Integrations");

      default -> List.of();
    };
  }
}

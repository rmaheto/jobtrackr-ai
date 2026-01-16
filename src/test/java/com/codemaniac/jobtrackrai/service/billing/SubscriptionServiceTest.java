package com.codemaniac.jobtrackrai.service.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

  @Mock private SubscriptionRepository subscriptionRepository;
  @Mock private PlanRepository planRepository;
  @Mock private UserRepository userRepository;

  @Mock private Event event;

  private SubscriptionService service;

  @BeforeEach
  void setUp() {
    service = new SubscriptionService(subscriptionRepository, planRepository, userRepository);
  }

  @Test
  void syncFromProvider_whenSubscriptionExists_updatesAndSaves() {

    final ProviderSubscription dto = providerDto("active");
    final Subscription existing = Subscription.builder().build();

    when(subscriptionRepository.findByProviderSubscriptionId(dto.subscriptionId()))
        .thenReturn(Optional.of(existing));

    try (final MockedStatic<StripeMapper> mapper = mockStatic(StripeMapper.class)) {
      mapper.when(() -> StripeMapper.toSubscription(event)).thenReturn(dto);

      service.syncFromProvider(event);

      assertEquals(SubscriptionStatus.ACTIVE, existing.getStatus());
      assertEquals(dto.currentPeriodStart(), existing.getCurrentPeriodStart());
      assertEquals(dto.currentPeriodEnd(), existing.getCurrentPeriodEnd());
      verify(subscriptionRepository).save(existing);
    }
  }

  @Test
  void syncFromProvider_whenSubscriptionDoesNotExist_createsNewAndSaves() {

    final ProviderSubscription dto = providerDto("trialing");
    final User user = new User();
    final Plan plan = new Plan();

    when(subscriptionRepository.findByProviderSubscriptionId(dto.subscriptionId()))
        .thenReturn(Optional.empty());
    when(userRepository.findByStripeCustomerId(dto.customerId())).thenReturn(Optional.of(user));
    when(planRepository.findByStripePriceId(dto.priceId())).thenReturn(Optional.of(plan));

    try (final MockedStatic<StripeMapper> mapper = mockStatic(StripeMapper.class)) {
      mapper.when(() -> StripeMapper.toSubscription(event)).thenReturn(dto);

      service.syncFromProvider(event);

      verify(subscriptionRepository).save(any(Subscription.class));
    }
  }

  @Test
  void syncFromProvider_whenUserNotFound_throwsException() {

    final ProviderSubscription dto = providerDto("active");

    when(subscriptionRepository.findByProviderSubscriptionId(dto.subscriptionId()))
        .thenReturn(Optional.empty());
    when(userRepository.findByStripeCustomerId(dto.customerId())).thenReturn(Optional.empty());

    try (final MockedStatic<StripeMapper> mapper = mockStatic(StripeMapper.class)) {
      mapper.when(() -> StripeMapper.toSubscription(event)).thenReturn(dto);

      final IllegalStateException ex =
          assertThrows(IllegalStateException.class, () -> service.syncFromProvider(event));

      assertTrue(ex.getMessage().contains("No user found"));
    }
  }

  @Test
  void syncFromProvider_whenPlanNotFound_throwsException() {

    final ProviderSubscription dto = providerDto("active");
    final User user = new User();

    when(subscriptionRepository.findByProviderSubscriptionId(dto.subscriptionId()))
        .thenReturn(Optional.empty());
    when(userRepository.findByStripeCustomerId(dto.customerId())).thenReturn(Optional.of(user));
    when(planRepository.findByStripePriceId(dto.priceId())).thenReturn(Optional.empty());

    try (final MockedStatic<StripeMapper> mapper = mockStatic(StripeMapper.class)) {
      mapper.when(() -> StripeMapper.toSubscription(event)).thenReturn(dto);

      final IllegalStateException ex =
          assertThrows(IllegalStateException.class, () -> service.syncFromProvider(event));

      assertTrue(ex.getMessage().contains("No plan mapped"));
    }
  }

  @Test
  void syncFromProvider_whenProviderStatusUnknown_throwsException() {

    final ProviderSubscription dto = providerDto("weird_status");

    when(subscriptionRepository.findByProviderSubscriptionId(dto.subscriptionId()))
        .thenReturn(Optional.of(new Subscription()));

    try (final MockedStatic<StripeMapper> mapper = mockStatic(StripeMapper.class)) {
      mapper.when(() -> StripeMapper.toSubscription(event)).thenReturn(dto);

      assertThrows(IllegalArgumentException.class, () -> service.syncFromProvider(event));
    }
  }

  @Test
  void cancelFromProvider_whenSubscriptionExists_marksCanceledAndSaves() {

    final ProviderSubscription dto = providerDto("canceled");
    final Subscription subscription = new Subscription();

    when(subscriptionRepository.findByProviderSubscriptionId(dto.subscriptionId()))
        .thenReturn(Optional.of(subscription));

    try (final MockedStatic<StripeMapper> mapper = mockStatic(StripeMapper.class)) {
      mapper.when(() -> StripeMapper.toSubscription(event)).thenReturn(dto);

      service.cancelFromProvider(event);

      assertEquals(SubscriptionStatus.CANCELED, subscription.getStatus());
      verify(subscriptionRepository).save(subscription);
    }
  }

  @Test
  void cancelFromProvider_whenSubscriptionNotFound_throwsException() {

    final ProviderSubscription dto = providerDto("canceled");

    when(subscriptionRepository.findByProviderSubscriptionId(dto.subscriptionId()))
        .thenReturn(Optional.empty());

    try (final MockedStatic<StripeMapper> mapper = mockStatic(StripeMapper.class)) {
      mapper.when(() -> StripeMapper.toSubscription(event)).thenReturn(dto);

      assertThrows(IllegalStateException.class, () -> service.cancelFromProvider(event));
    }
  }

  @Test
  void cancelFromProvider_whenPeriodEndProvided_setsPeriodEnd() {

    final ProviderSubscription dto = providerDto("canceled");
    final Subscription subscription = new Subscription();

    when(subscriptionRepository.findByProviderSubscriptionId(dto.subscriptionId()))
        .thenReturn(Optional.of(subscription));

    try (final MockedStatic<StripeMapper> mapper = mockStatic(StripeMapper.class)) {
      mapper.when(() -> StripeMapper.toSubscription(event)).thenReturn(dto);

      service.cancelFromProvider(event);

      assertEquals(dto.currentPeriodEnd(), subscription.getCurrentPeriodEnd());
    }
  }

  private ProviderSubscription providerDto(final String status) {
    return new ProviderSubscription(
        "sub_123",
        "cus_123",
        "price_123",
        status,
        Instant.now(),
        Instant.now().plusSeconds(30_000),
        false);
  }
}

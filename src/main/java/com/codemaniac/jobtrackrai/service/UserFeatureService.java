package com.codemaniac.jobtrackrai.service;

import com.codemaniac.jobtrackrai.entity.Plan;
import com.codemaniac.jobtrackrai.entity.Subscription;
import com.codemaniac.jobtrackrai.entity.User;
import com.codemaniac.jobtrackrai.enums.Feature;
import com.codemaniac.jobtrackrai.repository.PlanRepository;
import com.codemaniac.jobtrackrai.repository.SubscriptionRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserFeatureService {

  private final SubscriptionRepository subscriptionRepository;
  private final PlanRepository planRepository;
  private final Clock clock;

  /**
   * Returns the effective plan for a user. ACTIVE/TRIALING subscription → its plan Otherwise → FREE
   * plan
   */
  public Plan getEffectivePlan(final User user) {

    return subscriptionRepository
        .findActiveByUserId(user.getId(), Instant.now(clock))
        .map(Subscription::getPlan)
        .orElseGet(this::getFreePlan);
  }

  public Set<Feature> getFeatures(final User user) {
    return getEffectivePlan(user).getFeatures();
  }

  public boolean hasFeature(final User user, final Feature feature) {
    return getFeatures(user).contains(feature);
  }

  public Integer maxApplications(final User user) {
    return getEffectivePlan(user).getMaxApplications();
  }

  public Integer maxResumes(final User user) {
    return getEffectivePlan(user).getMaxResumes();
  }

  private Plan getFreePlan() {
    return planRepository
        .findByCodeAndActiveTrue("FREE")
        .orElseThrow(() -> new IllegalStateException("FREE plan not configured"));
  }
}

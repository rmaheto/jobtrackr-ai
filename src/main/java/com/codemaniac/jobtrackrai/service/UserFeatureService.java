package com.codemaniac.jobtrackrai.service;

import com.codemaniac.jobtrackrai.entity.Plan;
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

  private volatile Set<Feature> freeFeatures;

  public Set<Feature> getFeatures(final User user) {

    return subscriptionRepository
        .findActiveByUserId(user.getId(), Instant.now(clock))
        .map(sub -> sub.getPlan().getFeatures())
        .orElseGet(this::getFreeFeatures);
  }

  public boolean hasFeature(final User user, final Feature feature) {
    return getFeatures(user).contains(feature);
  }

  private Set<Feature> getFreeFeatures() {
    if (freeFeatures == null) {
      synchronized (this) {
        if (freeFeatures == null) {
          freeFeatures =
              planRepository
                  .findByCodeAndActiveTrue("FREE")
                  .map(Plan::getFeatures)
                  .orElse(Set.of());
        }
      }
    }
    return freeFeatures;
  }
}

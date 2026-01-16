package com.codemaniac.jobtrackrai.service.billing;

import com.codemaniac.jobtrackrai.entity.User;
import com.codemaniac.jobtrackrai.enums.PlanCode;
import com.codemaniac.jobtrackrai.repository.SubscriptionRepository;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserPlanService {

  private final SubscriptionRepository subscriptionRepository;
  private final Clock clock;

  public PlanCode getEffectivePlan(final User user) {
    return subscriptionRepository
        .findActiveByUserId(user.getId(), Instant.now(clock))
        .map(sub -> sub.getPlan().getCode())
        .orElse(PlanCode.FREE);
  }
}

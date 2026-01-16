package com.codemaniac.jobtrackrai.repository;

import com.codemaniac.jobtrackrai.entity.Subscription;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

  @Query(
      """
      select s
      from Subscription s
      where s.user.id = :userId
        and s.status in ('ACTIVE', 'TRIALING')
        and (s.currentPeriodEnd is null or s.currentPeriodEnd > :now)
  """)
  Optional<Subscription> findActiveByUserId(
      @Param("userId") Long userId, @Param("now") Instant now);

  Optional<Subscription> findByProviderSubscriptionId(String providerSubscriptionId);
}

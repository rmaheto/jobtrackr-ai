package com.codemaniac.jobtrackrai.entity;

import com.codemaniac.jobtrackrai.enums.SubscriptionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "subscriptions",
    indexes = {
      @Index(name = "idx_subscription_user", columnList = "user_id"),
      @Index(name = "idx_subscription_provider", columnList = "provider_subscription_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "plan_id", nullable = false)
  private Plan plan;

  @Column(name = "status", nullable = false)
  @Enumerated(EnumType.STRING)
  private SubscriptionStatus status;

  @Column(name = "provider_subscription_id", unique = true)
  private String providerSubscriptionId;

  @Column(name = "provider_customer_id")
  private String providerCustomerId;

  @Column(name = "current_period_start")
  private Instant currentPeriodStart;

  @Column(name = "current_period_end")
  private Instant currentPeriodEnd;

  @Column(name = "cancel_at_period_end", nullable = false)
  private boolean cancelAtPeriodEnd = false;
}

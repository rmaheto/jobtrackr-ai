package com.codemaniac.jobtrackrai.entity;

import com.codemaniac.jobtrackrai.enums.BillingInterval;
import com.codemaniac.jobtrackrai.enums.PlanCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "code", nullable = false, unique = true, length = 32)
  @Enumerated(EnumType.STRING)
  private PlanCode code;

  @Column(name = "stripe_product_id", length = 64)
  private String stripeProductId;

  @Column(nullable = false, length = 64)
  private String stripePriceId;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "price_amount", nullable = false)
  private Long priceAmount;

  @Column(name = "currency", nullable = false, length = 3)
  private String currency;

  @Column(name = "billing_interval", nullable = false)
  @Enumerated(EnumType.STRING)
  private BillingInterval billingInterval;

  @Column(name = "interval_count", nullable = false)
  private int intervalCount;

  @Column(name = "active", nullable = false)
  private boolean active = true;
}

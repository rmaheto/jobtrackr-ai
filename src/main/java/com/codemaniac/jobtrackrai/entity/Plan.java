package com.codemaniac.jobtrackrai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

  @Column(nullable = false, unique = true)
  private String code;

  @Column(name = "stripe_product_id", length = 64)
  private String stripeProductId;

  @Column(nullable = false, length = 64)
  private String stripePriceId;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "price_amount", nullable = false)
  private Long priceAmount; // cents

  @Column(name = "currency", nullable = false, length = 3)
  private String currency;

  @Column(name = "billing_interval", nullable = false)
  private String billingInterval;

  @Column(name = "interval_count", nullable = false)
  private Long intervalCount;

  @Column(name = "active", nullable = false)
  private boolean active = true;

  public void mergeFrom(final Plan source) {
    this.stripePriceId = source.stripePriceId;
    this.code = source.code;
    this.name = source.name;
    this.stripeProductId = source.stripeProductId;
    this.priceAmount = source.priceAmount;
    this.currency = source.currency;
    this.billingInterval = source.billingInterval;
    this.intervalCount = source.intervalCount;
    this.active = source.active;
  }
}

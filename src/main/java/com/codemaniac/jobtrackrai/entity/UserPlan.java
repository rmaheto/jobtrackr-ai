package com.codemaniac.jobtrackrai.entity;

import com.codemaniac.jobtrackrai.enums.PlanType;
import com.codemaniac.jobtrackrai.model.AuditableEntity;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

@Entity
@Table(name = "user_plans")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPlan extends AuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

  private String stripeCustomerId;
  private String stripeSubscriptionId;
  private String stripePriceId;

  @Enumerated(EnumType.STRING)
  private PlanType planType;

  private Instant startDate;
  private Instant endDate;
  private Instant currentPeriodEnd;

  private boolean active;
  private String status;
  private Instant cancelAt;
}

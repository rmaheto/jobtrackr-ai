package com.codemaniac.jobtrackrai.entity;

import com.codemaniac.jobtrackrai.enums.PlanType;
import com.codemaniac.jobtrackrai.model.AuditableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_plan_history")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPlanHistory extends AuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  private PlanType planType;

  private String stripeSubscriptionId;
  private String stripeInvoiceId;
  private String status;
  private Instant startDate;
  private Instant endDate;
  private Instant recordedAt;
  private String note;
}

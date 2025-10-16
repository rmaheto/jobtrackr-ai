package com.codemaniac.jobtrackrai.entity;

import com.codemaniac.jobtrackrai.enums.FollowUpType;
import com.codemaniac.jobtrackrai.interceptor.AuditInterceptor;
import com.codemaniac.jobtrackrai.model.Audit;
import com.codemaniac.jobtrackrai.model.Auditable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(AuditInterceptor.class)
public class FollowUp implements Auditable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private LocalDate date;
  private OffsetDateTime scheduledAt;

  @Enumerated(EnumType.STRING)
  private FollowUpType type;

  private String notes;
  private boolean completed = false;

  // provider-agnostic calendar reference
  private String calendarEventId;
  private String calendarProvider;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "job_application_id", nullable = false)
  private JobApplication jobApplication;

  @Embedded private Audit audit = new Audit();
}

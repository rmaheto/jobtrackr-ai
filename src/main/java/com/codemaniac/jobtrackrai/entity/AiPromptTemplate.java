package com.codemaniac.jobtrackrai.entity;

import com.codemaniac.jobtrackrai.interceptor.AuditInterceptor;
import com.codemaniac.jobtrackrai.model.Audit;
import com.codemaniac.jobtrackrai.model.Auditable;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"code"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(AuditInterceptor.class)
public class AiPromptTemplate implements Auditable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String code;

  @Column(columnDefinition = "TEXT")
  private String template;

  private String model;

  @Embedded
  private Audit audit = new Audit();
}

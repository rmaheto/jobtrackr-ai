package com.codemaniac.jobtrackrai.entity;

import com.codemaniac.jobtrackrai.interceptor.AuditInterceptor;
import com.codemaniac.jobtrackrai.model.Audit;
import com.codemaniac.jobtrackrai.model.Auditable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(AuditInterceptor.class)
public class User implements Auditable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String externalId;

  @Column(name = "stripe_customer_id", unique = true)
  private String stripeCustomerId;

  private String email;
  private String name;
  private String pictureUrl;
  private String provider;

  @Column(length = 2048)
  private String googleAccessToken;

  @Column(length = 2048)
  private String googleRefreshToken;

  private Long tokenExpiry;

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  @ToString.Exclude
  @JsonIgnore
  private List<JobApplication> applications = new ArrayList<>();

  @Embedded private Audit audit = new Audit();
}

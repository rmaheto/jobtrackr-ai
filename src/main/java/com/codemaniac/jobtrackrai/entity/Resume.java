package com.codemaniac.jobtrackrai.entity;

import com.codemaniac.jobtrackrai.enums.ResumeFileType;
import com.codemaniac.jobtrackrai.interceptor.AuditInterceptor;
import com.codemaniac.jobtrackrai.model.Audit;
import com.codemaniac.jobtrackrai.model.Auditable;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "resumes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(AuditInterceptor.class)
public class Resume implements Auditable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String originalName;

  private String s3Key;

  @Enumerated(EnumType.STRING)
  private ResumeFileType fileType;

  private Long size;

  private Integer linkedApplications = 0;

  @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = false)
  private List<JobApplication> jobApplications = new ArrayList<>();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @Embedded private Audit audit = new Audit();

  public void addJobApplication(final JobApplication application) {
    jobApplications.add(application);
    application.setResume(this);
    this.linkedApplications = (this.linkedApplications == null ? 1 : this.linkedApplications + 1);
  }

  public void removeJobApplication(final JobApplication application) {
    jobApplications.remove(application);
    application.setResume(null);
    this.linkedApplications = Math.max(0, this.linkedApplications - 1);
  }
}

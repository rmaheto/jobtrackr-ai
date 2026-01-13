package com.codemaniac.jobtrackrai.model;

import com.codemaniac.jobtrackrai.interceptor.AuditInterceptor;
import jakarta.persistence.Embedded;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
@EntityListeners(AuditInterceptor.class)
public abstract class AuditableEntity implements Auditable {

  @Embedded protected Audit audit = new Audit();

  @Override
  public Audit getAudit() {
    return audit;
  }

  @Override
  public void setAudit(final Audit audit) {
    this.audit = audit;
  }
}

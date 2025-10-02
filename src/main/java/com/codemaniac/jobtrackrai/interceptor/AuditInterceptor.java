package com.codemaniac.jobtrackrai.interceptor;

import com.codemaniac.jobtrackrai.model.Audit;
import com.codemaniac.jobtrackrai.model.Auditable;
import com.codemaniac.jobtrackrai.util.SecurityUtils;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

@Component
public class AuditInterceptor {

  @PrePersist
  public void setCreationAudit(final Object entity) {
    if (entity instanceof final Auditable auditable && ObjectUtils.isEmpty(auditable.getAudit())) {
        auditable.setAudit(new Audit(SecurityUtils.getCurrentUsername(), Audit.PROGRAM));
      }

  }

  @PreUpdate
  public void setUpdateAudit(final Object entity) {
    if (entity instanceof final Auditable auditable) {
      Audit audit = auditable.getAudit();
      if (ObjectUtils.isEmpty(audit)) {
        audit = new Audit();
      }
      audit.setUpdates(SecurityUtils.getCurrentUsername(), Audit.PROGRAM);
    }
  }
}

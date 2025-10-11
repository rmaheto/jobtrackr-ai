package com.codemaniac.jobtrackrai.model;

import java.io.Serializable;

public interface Auditable extends Serializable {
  Audit getAudit();

  void setAudit(Audit audit);
}

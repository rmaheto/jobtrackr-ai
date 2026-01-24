package com.codemaniac.jobtrackrai.exception;

public class ForbiddenException extends RuntimeException {
  public ForbiddenException(final String upgradeRequired) {
    super(upgradeRequired);
  }
}

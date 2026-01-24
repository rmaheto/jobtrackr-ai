package com.codemaniac.jobtrackrai.exception;

public class BillingException extends RuntimeException {

  public BillingException(final String message) {
    super(message);
  }

  public BillingException(final String message, final Throwable cause) {
    super(message, cause);
  }
}

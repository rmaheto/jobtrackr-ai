package com.codemaniac.jobtrackrai.exception;

public class PropertyEncryptionException extends RuntimeException {

  public PropertyEncryptionException(final String message, final Throwable e) {
    super(message, e);
  }

  public PropertyEncryptionException(final String message) {
    super(message);
  }
}

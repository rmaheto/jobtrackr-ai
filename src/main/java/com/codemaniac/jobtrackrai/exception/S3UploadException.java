package com.codemaniac.jobtrackrai.exception;

public class S3UploadException extends RuntimeException {
  public S3UploadException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public S3UploadException(final String message) {
    super(message);
  }
}

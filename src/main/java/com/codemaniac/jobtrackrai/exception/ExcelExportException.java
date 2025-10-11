package com.codemaniac.jobtrackrai.exception;

/** Thrown when Excel file generation or export fails. */
public class ExcelExportException extends RuntimeException {
  public ExcelExportException(final String message, final Throwable cause) {
    super(message, cause);
  }
}

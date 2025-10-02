package com.codemaniac.jobtrackrai.exception;

public class NotFoundException extends RuntimeException{

  public NotFoundException(final Long id) {
    super("JobApplication  with id " + id + " not found");
  }
}

package com.codemaniac.jobtrackrai.exception;

import com.codemaniac.jobtrackrai.model.ApiResponse;
import com.codemaniac.jobtrackrai.model.Error;
import com.codemaniac.jobtrackrai.model.Errors;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ApiExceptionHandler {

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<Errors> handleSchoolNotFound(final NotFoundException ex) {
    log.warn("School not found: {}", ex.getMessage());
    return build(HttpStatus.NOT_FOUND, "SCHOOL_NOT_FOUND", ex.getMessage(), null);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Errors> handleIllegalArgument(final IllegalArgumentException ex) {
    return build(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", ex.getMessage(), null);
  }

  @ExceptionHandler(InternalServerException.class)
  public ResponseEntity<Errors> handleInternalServerException(final InternalServerException ex) {
    return build(HttpStatus.INTERNAL_SERVER_ERROR, "GENERIC_ERROR", ex.getMessage(), null);
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<Errors> handleBadRequest(final BadRequestException ex) {
    return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), null);
  }

  @ExceptionHandler(BillingException.class)
  public ResponseEntity<ApiResponse<Void>> handleBilling(final BillingException ex) {
    return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
        .body(ApiResponse.of("ERROR", ex.getMessage(), null));
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<Errors> handleForbiddenException(final ForbiddenException ex) {
    return build(HttpStatus.FORBIDDEN, "FORBIDDEN", ex.getMessage(), null);
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity<Errors> handleOther(final Exception ex) {
    log.error("Unexpected error", ex);
    return build(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "GENERIC_ERROR",
        "An unexpected error occurred. Please contact support.",
        ex.getMessage());
  }

  private ResponseEntity<Errors> build(
      final HttpStatus status,
      final String code,
      final String localizedMessage,
      final String supportInformation) {
    final Error error =
        Error.builder()
            .code(code)
            .locale("eng-USA")
            .localizedMessage(localizedMessage)
            .severity(Error.Severity.ERROR)
            .supportInformation(supportInformation)
            .build();

    final Errors errors = Errors.builder().errors(List.of(error)).build();

    return ResponseEntity.status(status).body(errors);
  }
}

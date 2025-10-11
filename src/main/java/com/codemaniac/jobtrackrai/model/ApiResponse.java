package com.codemaniac.jobtrackrai.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
  private String code;
  private String message;
  private T data;

  public static <T> ApiResponse<T> of(final String code, final String message, final T data) {
    return ApiResponse.<T>builder().code(code).message(message).data(data).build();
  }

  public static <T> ApiResponse<T> of(final String code, final String message) {
    return of(code, message, null);
  }
}

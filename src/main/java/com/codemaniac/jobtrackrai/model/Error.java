package com.codemaniac.jobtrackrai.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Error implements Serializable {

  private static final long serialVersionUID = 1L;

  @NotNull
  private String code;

  @NotNull
  private String locale;

  @NotNull
  private String localizedMessage;

  private List<String> paths;

  @NotNull
  private Severity severity;

  private String supportInformation;

  public enum Severity {
    INFO,
    WARNING,
    ERROR
  }
}

package com.codemaniac.jobtrackrai.model;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Errors implements Serializable {

  private static final long serialVersionUID = 1L;

  @NotNull
  @Valid
  private List<Error> errors;
}

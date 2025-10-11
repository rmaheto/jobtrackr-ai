package com.codemaniac.jobtrackrai.dto;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobApplicationRequest {
  private Optional<String> company = Optional.empty();
  private Optional<String> role = Optional.empty();
  private Optional<String> location = Optional.empty();
  private Optional<String> jobType = Optional.empty();
  private Optional<String> skills = Optional.empty();
  private Optional<String> salary = Optional.empty();
  private Optional<String> jobLink = Optional.empty();
  private Optional<String> description = Optional.empty();
  private Optional<String> contactPersonName = Optional.empty();
  private Optional<String> contactPersonEmail = Optional.empty();
  private Optional<String> status = Optional.empty();
  private Optional<String> notes = Optional.empty();
  private Optional<Long> linkedResumeId = Optional.empty();
}

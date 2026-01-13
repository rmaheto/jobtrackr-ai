package com.codemaniac.jobtrackrai.dto;

import com.codemaniac.jobtrackrai.enums.JobSource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateJobApplicationFromIndeedRequest {

  @NotBlank private String jobLink;

  @NotNull private Long linkedResumeId;

  @NotNull private String appliedDate;

  private String company;
  private String role;
  private String description;
  private String skills;
  private String jobType;
  private String location;
  private String salary;
  private String notes;
  private String contactPersonName;
  private String contactPersonEmail;
  private Boolean scrapeFromUrl;
  private JobSource jobSource;
}

package com.codemaniac.jobtrackrai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class IndeedJobSnapshotResponse {

  @JsonProperty("jobid")
  private String jobId;

  @JsonProperty("company_name")
  private String companyName;

  @JsonProperty("job_title")
  private String jobTitle;

  @JsonProperty("description_text")
  private String descriptionText;

  @JsonProperty("job_description_formatted")
  private String jobDescriptionFormatted;

  private String description;

  private String location;

  private String region;

  private String country;

  @JsonProperty("job_type")
  private String jobType;

  @JsonProperty("salary_formatted")
  private String salaryFormatted;

  private List<String> benefits;

  @JsonProperty("shift_schedule")
  private List<String> shiftSchedule;

  @JsonProperty("date_posted")
  private String datePosted;

  @JsonProperty("date_posted_parsed")
  private Instant datePostedParsed;

  private boolean isExpired;

  private String url;

  @JsonProperty("company_link")
  private String companyLink;

  private String domain;

  @JsonProperty("logo_url")
  private String logoUrl;

  private Instant timestamp;
}

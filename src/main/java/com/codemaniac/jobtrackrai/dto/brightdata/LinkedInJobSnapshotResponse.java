package com.codemaniac.jobtrackrai.dto.brightdata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LinkedInJobSnapshotResponse {

  @JsonProperty("job_posting_id")
  private String jobPostingId;

  @JsonProperty("job_title")
  private String jobTitle;

  @JsonProperty("company_name")
  private String companyName;

  @JsonProperty("company_id")
  private String companyId;

  @JsonProperty("job_location")
  private String jobLocation;

  @JsonProperty("job_employment_type")
  private String employmentType;

  @JsonProperty("job_seniority_level")
  private String seniorityLevel;

  @JsonProperty("job_industries")
  private String industries;

  @JsonProperty("job_function")
  private String jobFunction;

  @JsonProperty("job_summary")
  private String jobSummary;

  @JsonProperty("job_description_formatted")
  private String jobDescriptionFormatted;

  @JsonProperty("base_salary")
  private LinkedInSalary baseSalary;

  @JsonProperty("job_posted_date")
  private Instant jobPostedDate;

  private String url;
  private String applyLink;
  private Instant timestamp;
}

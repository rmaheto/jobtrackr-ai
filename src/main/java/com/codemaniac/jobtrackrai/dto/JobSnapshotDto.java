package com.codemaniac.jobtrackrai.dto;

import java.time.Instant;
import java.util.List;
import lombok.Data;

@Data
public class JobSnapshotDto {

  private String externalJobId;

  private String jobTitle;

  private String jobDescription;

  private String companyName;

  private String location;

  private String jobType;

  private String salary;

  private String jobUrl;

  private Instant datePosted;

  private Instant scrapedAt;

  private List<String> benefits;
}

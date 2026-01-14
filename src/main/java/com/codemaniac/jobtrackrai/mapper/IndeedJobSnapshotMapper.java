package com.codemaniac.jobtrackrai.mapper;

import com.codemaniac.jobtrackrai.dto.JobSnapshotDto;
import com.codemaniac.jobtrackrai.dto.brightdata.IndeedJobSnapshotResponse;
import java.time.Instant;

public final class IndeedJobSnapshotMapper {

  private IndeedJobSnapshotMapper() {
    // utility class
  }

  public static JobSnapshotDto toJobSnapshotDto(final IndeedJobSnapshotResponse source) {
    if (source == null) {
      return null;
    }

    final JobSnapshotDto dto = new JobSnapshotDto();

    dto.setExternalJobId(source.getJobId());
    dto.setJobTitle(source.getJobTitle());
    dto.setCompanyName(source.getCompanyName());
    dto.setLocation(source.getLocation());
    dto.setJobType(source.getJobType());
    dto.setSalary(source.getSalaryFormatted());
    dto.setJobUrl(source.getUrl());
    dto.setDatePosted(source.getDatePostedParsed());
    dto.setScrapedAt(Instant.now());
    dto.setJobDescription(source.getDescription());
    dto.setBenefits(source.getBenefits());

    return dto;
  }
}

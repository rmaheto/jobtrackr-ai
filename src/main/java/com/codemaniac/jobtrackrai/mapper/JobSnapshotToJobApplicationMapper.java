package com.codemaniac.jobtrackrai.mapper;

import com.codemaniac.jobtrackrai.dto.IndeedJobSnapshotResponse;
import com.codemaniac.jobtrackrai.entity.JobApplication;
import org.apache.commons.lang3.StringUtils;

public final class JobSnapshotToJobApplicationMapper {

  private JobSnapshotToJobApplicationMapper() {}

  public static void merge(final IndeedJobSnapshotResponse snapshot, final JobApplication target) {

    if (StringUtils.isEmpty(target.getCompany())) {
      target.setCompany(snapshot.getCompanyName());
    }

    if (StringUtils.isEmpty(target.getRole())) {
      target.setRole(snapshot.getJobTitle());
    }

    if (StringUtils.isEmpty(target.getLocation())) {
      target.setLocation(snapshot.getLocation());
    }

    if (StringUtils.isEmpty(target.getJobType())) {
      target.setJobType(snapshot.getJobType());
    }

    if (StringUtils.isEmpty(target.getSalary())) {
      target.setSalary(snapshot.getSalaryFormatted());
    }

    if (StringUtils.isEmpty(target.getDescription())) {
      target.setDescription(snapshot.getDescription());
    }
  }
}

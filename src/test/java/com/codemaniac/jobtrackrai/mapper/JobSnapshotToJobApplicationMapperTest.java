package com.codemaniac.jobtrackrai.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.codemaniac.jobtrackrai.dto.IndeedJobSnapshotResponse;
import com.codemaniac.jobtrackrai.entity.JobApplication;
import org.junit.jupiter.api.Test;

class JobSnapshotToJobApplicationMapperTest {

  @Test
  void merge_whenTargetFieldsAreNull_populatesFromSnapshot() {
    final JobApplication job = new JobApplication();

    final IndeedJobSnapshotResponse snapshot = new IndeedJobSnapshotResponse();
    snapshot.setCompanyName("Acme Corp");
    snapshot.setJobTitle("Senior Engineer");
    snapshot.setLocation("Remote");
    snapshot.setSalaryFormatted("$120k");

    JobSnapshotToJobApplicationMapper.merge(snapshot, job);

    assertEquals("Acme Corp", job.getCompany());
    assertEquals("Senior Engineer", job.getRole());
    assertEquals("Remote", job.getLocation());
    assertEquals("$120k", job.getSalary());
  }

  @Test
  void merge_whenUserFieldsAlreadySet_doesNotOverwriteExistingValues() {
    final JobApplication job = new JobApplication();
    job.setCompany("User Company");
    job.setRole("User Role");

    final IndeedJobSnapshotResponse snapshot = new IndeedJobSnapshotResponse();
    snapshot.setCompanyName("Scraped Company");
    snapshot.setJobTitle("Scraped Role");

    JobSnapshotToJobApplicationMapper.merge(snapshot, job);

    assertEquals("User Company", job.getCompany());
    assertEquals("User Role", job.getRole());
  }
}

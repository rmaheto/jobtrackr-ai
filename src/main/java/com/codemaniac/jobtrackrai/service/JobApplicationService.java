package com.codemaniac.jobtrackrai.service;

import com.codemaniac.jobtrackrai.dto.JobApplicationDto;
import com.codemaniac.jobtrackrai.dto.JobApplicationRequest;
import com.codemaniac.jobtrackrai.dto.JobApplicationSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface JobApplicationService {
  JobApplicationDto create(JobApplicationRequest request);
  JobApplicationRequest createFromLink(String jobUrl);
  Page<JobApplicationDto> search(final JobApplicationSearchRequest request, final Pageable pageable);
  JobApplicationDto findById(Long id);
  JobApplicationDto update(Long id, JobApplicationRequest request);
  JobApplicationDto updateStatus(Long id, String status);
  void delete(Long id);
  // JobApplicationService.java
  public byte[] exportToExcel(JobApplicationSearchRequest request);

}

package com.codemaniac.jobtrackrai.controller;

import com.codemaniac.jobtrackrai.dto.JobApplicationDto;
import com.codemaniac.jobtrackrai.dto.JobApplicationRequest;
import com.codemaniac.jobtrackrai.dto.JobApplicationSearchRequest;
import com.codemaniac.jobtrackrai.enums.Status;
import com.codemaniac.jobtrackrai.model.ApiResponse;
import com.codemaniac.jobtrackrai.service.JobApplicationService;
import java.util.Arrays;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/applications")
@AllArgsConstructor
public class JobApplicationController {

  private final JobApplicationService jobApplicationService;

  @PostMapping
  public ResponseEntity<ApiResponse<JobApplicationDto>> createJobApplication(
      @RequestBody final JobApplicationRequest request) {

    final JobApplicationDto jobApplicationDto = jobApplicationService.create(request);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            ApiResponse.of(
                HttpStatus.CREATED.name(), "Job application created", jobApplicationDto));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<JobApplicationDto>> getJobApplication(
      @PathVariable final Long id) {
    final JobApplicationDto jobApplicationDto = jobApplicationService.findById(id);

    return ResponseEntity.status(HttpStatus.OK)
        .body(ApiResponse.of(HttpStatus.OK.name(), "Job application found", jobApplicationDto));
  }

  @PostMapping("/ai")
  public ResponseEntity<ApiResponse<JobApplicationRequest>> getJobApplicationRequestFromLink(
      @RequestParam final String jobUrl) {

    final JobApplicationRequest request = jobApplicationService.createFromLink(jobUrl);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            ApiResponse.of(
                HttpStatus.CREATED.name(), "Job application created from link", request));
  }

  @PostMapping("/search")
  public ResponseEntity<ApiResponse<Page<JobApplicationDto>>> searchApplications(
      @RequestBody final JobApplicationSearchRequest request,
      @RequestParam(defaultValue = "0") final int page,
      @RequestParam(defaultValue = "10") final int size,
      @RequestParam(defaultValue = "appliedDate") final String sortBy,
      @RequestParam(defaultValue = "desc") final String direction) {

    final Sort sort =
        direction.equalsIgnoreCase("desc")
            ? Sort.by(sortBy).descending().and(Sort.by("id").descending())
            : Sort.by(sortBy).ascending().and(Sort.by("id").ascending());

    final Pageable pageable = PageRequest.of(page, size, sort);

    final Page<JobApplicationDto> result = jobApplicationService.search(request, pageable);

    return ResponseEntity.ok(
        ApiResponse.of(HttpStatus.OK.name(), "Job applications retrieved", result));
  }

  @PatchMapping("{id}")
  public ResponseEntity<ApiResponse<JobApplicationDto>> updateJobApplication(
      @PathVariable final Long id, @RequestBody final JobApplicationRequest request) {

    final JobApplicationDto jobApplicationDto = jobApplicationService.update(id, request);
    return ResponseEntity.accepted()
        .body(ApiResponse.of(HttpStatus.OK.name(), "Job application updated", jobApplicationDto));
  }

  @PatchMapping("/{id}/status")
  public ResponseEntity<ApiResponse<JobApplicationDto>> updateStatus(
      @PathVariable final Long id, @RequestBody final Map<String, String> payload) {

    final String status = payload.get("status");
    if (status == null || status.isBlank()) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.of(HttpStatus.BAD_REQUEST.name(), "Status must not be empty", null));
    }

    try {
      final Status enumStatus = Status.valueOf(status.toUpperCase());
      final JobApplicationDto updated = jobApplicationService.updateStatus(id, enumStatus.name());

      return ResponseEntity.ok(
          ApiResponse.of(HttpStatus.OK.name(), "Job application status updated", updated));

    } catch (final IllegalArgumentException ex) {
      return ResponseEntity.badRequest()
          .body(
              ApiResponse.of(
                  HttpStatus.BAD_REQUEST.name(),
                  "Invalid status. Allowed values: " + Arrays.toString(Status.values()),
                  null));
    }
  }

  @DeleteMapping("{id}")
  public ResponseEntity<ApiResponse<Void>> deleteJobApplication(@PathVariable final Long id) {
    jobApplicationService.delete(id);

    return ResponseEntity.ok()
        .body(ApiResponse.of(HttpStatus.OK.name(), "Job application deleted"));
  }
  
  @GetMapping("/export")
  public ResponseEntity<byte[]> exportApplicationsToExcel(
      @ModelAttribute final JobApplicationSearchRequest request) {

    final byte[] excelData = jobApplicationService.exportToExcel(request);

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=job_applications.xlsx")
        .contentType(
            MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .body(excelData);
  }
}

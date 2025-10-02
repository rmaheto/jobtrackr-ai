package com.codemaniac.jobtrackrai.service;

import com.codemaniac.jobtrackrai.dto.JobApplicationDto;
import com.codemaniac.jobtrackrai.dto.JobApplicationRequest;
import com.codemaniac.jobtrackrai.dto.JobApplicationSearchRequest;
import com.codemaniac.jobtrackrai.entity.JobApplication;
import com.codemaniac.jobtrackrai.entity.User;
import com.codemaniac.jobtrackrai.enums.Status;
import com.codemaniac.jobtrackrai.exception.BadRequestException;
import com.codemaniac.jobtrackrai.exception.NotFoundException;
import com.codemaniac.jobtrackrai.mapper.JobApplicationMapper;
import com.codemaniac.jobtrackrai.model.Audit;
import com.codemaniac.jobtrackrai.repository.JobApplicationRepository;
import com.codemaniac.jobtrackrai.repository.JobApplicationSpecifications;
import jakarta.annotation.Nonnull;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@AllArgsConstructor
public class JobApplicationServiceImpl implements JobApplicationService {

  private final JobApplicationRepository repository;
  private final CurrentUserService currentUserService;
  private final JobScraperService jobScraperService;
  private final JobApplicationAiService jobApplicationAiService;
  private final JobApplicationMapper mapper;

  private static final String JOB_NOT_FOUND = "Job application not found id={}";

  @Override
  public JobApplicationDto create(final JobApplicationRequest request) {
    final User user = currentUserService.getCurrentUser();

    log.info(
        "Creating job application for user={} with company={}, role={}",
        user.getEmail(),
        request.getCompany(),
        request.getRole());

    final JobApplication entity = mapper.toEntity(request);
    entity.setStatus(Status.APPLIED);
    entity.setAppliedDate(LocalDate.now());
    entity.setUser(user);

    final JobApplication saved = repository.save(entity);

    if (log.isDebugEnabled()) {
      log.debug("Job application persisted with id={} for user={}", saved.getId(), user.getEmail());
    }

    return mapper.toDto(saved);
  }

  @Override
  public JobApplicationRequest createFromLink(@Nonnull final String jobUrl) {

    try {
      final String jobText = jobScraperService.extractJobText(jobUrl);

      return jobApplicationAiService.extractFromUrl(jobUrl, jobText);
    } catch (final Exception exception) {

      log.warn("Could not extract job text from link={}", jobUrl);

      throw exception;
    }
  }

  @Transactional(readOnly = true)
  @Override
  public Page<JobApplicationDto> search(
      final JobApplicationSearchRequest request, final Pageable pageable) {
    final User user = currentUserService.getCurrentUser();
    log.info("Searching job applications for userId={} with filters={}", user.getId(), request);

    return repository
        .findAll(JobApplicationSpecifications.forSearch(request, user.getId()), pageable)
        .map(mapper::toDto);
  }

  @Override
  public JobApplicationDto findById(final Long id) {
    return repository
        .findById(id)
        .filter(app -> Audit.RECORD_STATUS_ACTIVE.equals(app.getAudit().getRecordStatus()))
        .map(
            app -> {
              if (log.isDebugEnabled()) {
                log.debug(
                    "Job application found id={} company={} role={}",
                    app.getId(),
                    app.getCompany(),
                    app.getRole());
              }
              return mapper.toDto(app);
            })
        .orElseThrow(
            () -> {
              log.error(JOB_NOT_FOUND, id);
              return new NotFoundException(id);
            });
  }

  @Transactional
  @Override
  public JobApplicationDto update(final Long id, final JobApplicationRequest request) {

    final JobApplication jobApplication =
        findJobApplication(id).orElseThrow(() -> new NotFoundException(id));

    request.getCompany().ifPresent(jobApplication::setCompany);
    request.getJobType().ifPresent(jobApplication::setJobType);
    request.getLocation().ifPresent(jobApplication::setLocation);
    request.getDescription().ifPresent(jobApplication::setDescription);
    request.getSkills().ifPresent(jobApplication::setSkills);
    request.getRole().ifPresent(jobApplication::setRole);
    request.getJobLink().ifPresent(jobApplication::setJobLink);
    request.getSalary().ifPresent(jobApplication::setSalary);
    request.getNotes().ifPresent(jobApplication::setNotes);
    request
        .getStatus()
        .ifPresent(
            statusStr -> {
              try {
                jobApplication.setStatus(Status.valueOf(statusStr.toUpperCase()));
              } catch (final IllegalArgumentException ex) {
                log.warn(
                    "Invalid job status={} for application update with job id: {} with request: {}",
                    statusStr,
                    id,
                    request);
                throw new BadRequestException(
                    "Invalid status: "
                        + statusStr
                        + ". Allowed values: "
                        + Arrays.toString(Status.values()));
              }
            });

    return mapper.toDto(jobApplication);
  }

  @Override
  @Transactional
  public JobApplicationDto updateStatus(@Nonnull final Long id, @Nonnull final String status) {

    final JobApplication jobApplication =
        findJobApplication(id).orElseThrow(() -> new NotFoundException(id));

    jobApplication.setStatus(Status.valueOf(status));

    return mapper.toDto(jobApplication);
  }

  @Override
  @Transactional
  public void delete(final Long id) {

    findJobApplication(id)
        .ifPresentOrElse(
            this::setActiveRecordStatusToInactive,
            () -> log.warn("Attempted to delete non-existent job application id={}", id));
  }

  private Optional<JobApplication> findJobApplication(final Long id) {
    return repository
        .findById(id)
        .filter(app -> Audit.RECORD_STATUS_ACTIVE.equals(app.getAudit().getRecordStatus()))
        .map(
            app -> {
              if (log.isDebugEnabled()) {
                log.debug(
                    "Active job application found id={} company={} role={}",
                    app.getId(),
                    app.getCompany(),
                    app.getRole());
              }
              return app;
            });
  }

  private void setActiveRecordStatusToInactive(final JobApplication jobApplication) {
    jobApplication.getAudit().setRecordStatus(Audit.RECORD_STATUS_DELETED);
  }
}

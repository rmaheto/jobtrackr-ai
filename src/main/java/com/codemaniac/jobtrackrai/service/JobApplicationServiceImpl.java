package com.codemaniac.jobtrackrai.service;

import com.codemaniac.jobtrackrai.dto.CreateJobApplicationFromIndeedRequest;
import com.codemaniac.jobtrackrai.dto.JobApplicationDto;
import com.codemaniac.jobtrackrai.dto.JobApplicationRequest;
import com.codemaniac.jobtrackrai.dto.JobApplicationSearchRequest;
import com.codemaniac.jobtrackrai.enrichment.JobEnrichmentOrchestrator;
import com.codemaniac.jobtrackrai.entity.JobApplication;
import com.codemaniac.jobtrackrai.entity.Resume;
import com.codemaniac.jobtrackrai.entity.User;
import com.codemaniac.jobtrackrai.entity.UserPreference;
import com.codemaniac.jobtrackrai.enums.Status;
import com.codemaniac.jobtrackrai.exception.BadRequestException;
import com.codemaniac.jobtrackrai.exception.ExcelExportException;
import com.codemaniac.jobtrackrai.exception.NotFoundException;
import com.codemaniac.jobtrackrai.factory.DateRepresentationFactory;
import com.codemaniac.jobtrackrai.mapper.JobApplicationMapper;
import com.codemaniac.jobtrackrai.model.Audit;
import com.codemaniac.jobtrackrai.repository.JobApplicationRepository;
import com.codemaniac.jobtrackrai.repository.JobApplicationSpecifications;
import com.codemaniac.jobtrackrai.repository.ResumeRepository;
import com.codemaniac.jobtrackrai.service.brightdata.BrightDataService;
import com.codemaniac.jobtrackrai.util.IndeedJobUrlValidator;
import jakarta.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@AllArgsConstructor
public class JobApplicationServiceImpl implements JobApplicationService {

  private final JobApplicationRepository repository;
  private final ResumeRepository resumeRepository;
  private final CurrentUserService currentUserService;
  private final JobScraperService jobScraperService;
  private final JobApplicationAiService jobApplicationAiService;
  private final UserPreferenceService userPreferenceService;
  private final BrightDataService brightDataService;
  private final JobApplicationMapper jobApplicationMapper;
  private final DateRepresentationFactory dateRepresentationFactory;
  private final JobEnrichmentOrchestrator jobEnrichmentOrchestrator;

  private static final String JOB_NOT_FOUND = "Job application not found id={}";
  private static final String INVALID_RESUME_ID = "Invalid resume id id={}";

  @Override
  @Transactional
  public JobApplicationDto create(final JobApplicationRequest request) {

    final User user = currentUserService.getCurrentUser();
    final UserPreference pref = userPreferenceService.getUserPreferences();

    log.debug("Creating job application for user={} with request{}", user.getEmail(), request);

    final Resume resume =
        request
            .getLinkedResumeId()
            .map(
                resumeId ->
                    resumeRepository
                        .findById(resumeId)
                        .filter(r -> r.getUser().equals(user))
                        .orElseThrow(() -> new IllegalArgumentException(INVALID_RESUME_ID)))
            .orElse(null);

    final JobApplication jobApplication = jobApplicationMapper.toEntity(request, resume);

    jobApplication.setUser(user);
    jobApplication.setStatus(Status.APPLIED);
    jobApplication.setAppliedDate(
        request
            .getAppliedDate()
            .map(dateRepresentationFactory::parseFrontendLocalDate)
            .orElse(LocalDate.now()));

    final JobApplication saved = repository.save(jobApplication);

    if (log.isDebugEnabled()) {
      log.debug("Job application persisted with id={} for user={}", saved.getId(), user.getEmail());
    }
    jobEnrichmentOrchestrator.maybeEnrich(saved, request);

    return jobApplicationMapper.toDto(saved, pref);
  }


  @Transactional
  public JobApplicationDto createFromIndeed(final CreateJobApplicationFromIndeedRequest request) {
    final User user = currentUserService.getCurrentUser();
    final UserPreference pref = userPreferenceService.getUserPreferences();

    final String jobUrl = request.getJobLink();

    if (!IndeedJobUrlValidator.isValid(jobUrl)) {
      throw new IllegalArgumentException("Invalid Indeed job URL");
    }

    final Resume resume =
        resumeRepository
            .findById(request.getLinkedResumeId())
            .filter(r -> r.getUser().equals(user))
            .orElseThrow(() -> new IllegalArgumentException(INVALID_RESUME_ID));

    final JobApplication jobApplication = jobApplicationMapper.toEntity(request, resume);
    jobApplication.setUser(user);
    jobApplication.setAppliedDate(
        dateRepresentationFactory.parseFrontendLocalDate(request.getAppliedDate()));

    repository.save(jobApplication);

    final String snapshotId = brightDataService.createSnapshot(jobUrl).getSnapshotId();

    jobApplication.setSnapshotId(snapshotId);

    return jobApplicationMapper.toDto(jobApplication, pref);
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
    final UserPreference pref = userPreferenceService.getUserPreferences();

    log.info("Searching job applications for userId={} with filters={}", user.getId(), request);

    return repository
        .findAll(JobApplicationSpecifications.forSearch(request, user.getId()), pageable)
        .map(jobApplication -> jobApplicationMapper.toDto(jobApplication, pref));
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
              return jobApplicationMapper.toDto(app, userPreferenceService.getUserPreferences());
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
    final User user = currentUserService.getCurrentUser();
    final UserPreference pref = userPreferenceService.getUserPreferences();
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
        .getAppliedDate()
        .ifPresent(
            dateStr ->
                jobApplication.setAppliedDate(
                    dateRepresentationFactory.parseFrontendLocalDate(dateStr)));
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

    if (request.getLinkedResumeId().isPresent()) {
      final Long newResumeId =
          request
              .getLinkedResumeId()
              .orElseThrow(() -> new IllegalArgumentException("Linked resume ID is required"));

      if (jobApplication.getResume() != null
          && !jobApplication.getResume().getId().equals(newResumeId)) {
        jobApplication.getResume().removeJobApplication(jobApplication);
      }

      final Resume newResume =
          resumeRepository
              .findById(newResumeId)
              .filter(r -> r.getUser().equals(user))
              .orElseThrow(
                  () -> new IllegalArgumentException("Invalid resume ID or not owned by user"));

      newResume.addJobApplication(jobApplication);
    } else if (jobApplication.getResume() != null) {
      jobApplication.getResume().removeJobApplication(jobApplication);
    }

    return jobApplicationMapper.toDto(jobApplication, pref);
  }

  @Override
  @Transactional
  public JobApplicationDto updateStatus(@Nonnull final Long id, @Nonnull final String status) {

    final JobApplication jobApplication =
        findJobApplication(id).orElseThrow(() -> new NotFoundException(id));

    jobApplication.setStatus(Status.valueOf(status));

    return jobApplicationMapper.toDto(jobApplication, userPreferenceService.getUserPreferences());
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

  @Override
  public byte[] exportToExcel(final JobApplicationSearchRequest request) {
    final User user = currentUserService.getCurrentUser();
    final List<JobApplication> apps =
        repository.findAll(JobApplicationSpecifications.forSearch(request, user.getId()));

    try (final Workbook workbook = new XSSFWorkbook()) {
      final Sheet sheet = workbook.createSheet("Job Applications");

      createHeaderRow(workbook, sheet);
      populateDataRows(sheet, apps);
      autoSizeColumns(sheet, 7);

      return writeWorkbookToBytes(workbook);

    } catch (final IOException e) {
      throw new ExcelExportException("Failed to generate Excel file", e);
    }
  }

  private void createHeaderRow(final Workbook workbook, final Sheet sheet) {
    final String[] headers = {
      "Company", "Role", "Location", "Type", "Status", "Applied Date", "Job URL"
    };

    final Row headerRow = sheet.createRow(0);
    final CellStyle headerStyle = createHeaderStyle(workbook);

    for (int i = 0; i < headers.length; i++) {
      final Cell cell = headerRow.createCell(i);
      cell.setCellValue(headers[i]);
      cell.setCellStyle(headerStyle);
    }
  }

  private CellStyle createHeaderStyle(final Workbook workbook) {
    final Font font = workbook.createFont();
    font.setBold(true);

    final CellStyle style = workbook.createCellStyle();
    style.setFont(font);
    return style;
  }

  private void populateDataRows(final Sheet sheet, final List<JobApplication> apps) {
    int rowNum = 1;
    for (final JobApplication app : apps) {
      final Row row = sheet.createRow(rowNum++);
      row.createCell(0).setCellValue(defaultString(app.getCompany()));
      row.createCell(1).setCellValue(defaultString(app.getRole()));
      row.createCell(2).setCellValue(defaultString(app.getLocation()));
      row.createCell(3).setCellValue(defaultString(app.getJobType()));
      row.createCell(4).setCellValue(app.getStatus().name());
      row.createCell(5)
          .setCellValue(app.getAppliedDate() != null ? app.getAppliedDate().toString() : "");
      row.createCell(6).setCellValue(defaultString(app.getJobLink()));
    }
  }

  private void autoSizeColumns(final Sheet sheet, int totalColumns) {
    for (int i = 0; i < totalColumns; i++) {
      sheet.autoSizeColumn(i);
    }
  }

  private String defaultString(final String value) {
    return value != null ? value : "";
  }

  private byte[] writeWorkbookToBytes(final Workbook workbook) throws IOException {
    try (final ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      workbook.write(out);
      return out.toByteArray();
    }
  }

  private void setActiveRecordStatusToInactive(final JobApplication jobApplication) {
    jobApplication.getAudit().setRecordStatus(Audit.RECORD_STATUS_DELETED);
  }
}

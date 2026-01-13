package com.codemaniac.jobtrackrai.mapper;

import com.codemaniac.jobtrackrai.dto.*;
import com.codemaniac.jobtrackrai.entity.JobApplication;
import com.codemaniac.jobtrackrai.entity.Resume;
import com.codemaniac.jobtrackrai.entity.UserPreference;
import com.codemaniac.jobtrackrai.enums.EnrichmentStatus;
import com.codemaniac.jobtrackrai.enums.Status;
import com.codemaniac.jobtrackrai.factory.DateRepresentationFactory;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JobApplicationMapper {

  private final DateRepresentationFactory dateFactory;

  public JobApplicationDto toDto(final JobApplication entity, final UserPreference userPreference) {
    if (entity == null) return null;

    final EnrichmentStatus enrichmentStatus = entity.getEnrichmentStatus();

    return JobApplicationDto.builder()
        .id(entity.getId())
        .company(normalizeIfPending(entity.getCompany(), enrichmentStatus))
        .role(normalizeIfPending(entity.getRole(), enrichmentStatus))
        .location(normalizeIfPending(entity.getLocation(), enrichmentStatus))
        .description(normalizeIfPending(entity.getDescription(), enrichmentStatus))
        .notes(entity.getNotes()) // notes are user-owned; never suppressed
        .jobType(normalizeIfPending(entity.getJobType(), enrichmentStatus))
        .salary(normalizeIfPending(entity.getSalary(), enrichmentStatus))
        .skills(normalizeIfPending(entity.getSkills(), enrichmentStatus))
        .contactPersonName(entity.getContactPersonName())
        .contactPersonEmail(entity.getContactPersonEmail())
        .jobLink(entity.getJobLink())
        .status(entity.getStatus().name())
        .enrichmentStatus(enrichmentStatus.name())
        .appliedDate(
            entity.getAppliedDate() != null
                ? dateFactory.create(
                    entity.getAppliedDate().atStartOfDay().toInstant(ZoneOffset.UTC),
                    userPreference)
                : null)
        .linkedResumeId(entity.getResume() != null ? entity.getResume().getId() : null)
        .build();
  }

  public JobApplicationSummaryDto toSummaryDto(
      final JobApplication entity, final UserPreference userPreference) {
    if (entity == null) return null;

    return new JobApplicationSummaryDto(
        entity.getId(),
        entity.getCompany(),
        entity.getRole(),
        entity.getStatus(),
        entity.getAppliedDate() != null
            ? dateFactory.create(
                entity.getAppliedDate().atStartOfDay().toInstant(ZoneOffset.UTC), userPreference)
            : null);
  }

  public JobApplication toEntity(final JobApplicationRequest request, final Resume linkedResume) {
    if (request == null) return null;

    final JobApplication job = new JobApplication();

    job.setCompany(request.getCompany().orElse(null));
    job.setDescription(request.getDescription().orElse(null));
    job.setNotes(request.getNotes().orElse(null));
    job.setJobType(request.getJobType().orElse(null));
    job.setRole(request.getRole().orElse(null));
    job.setLocation(request.getLocation().orElse(null));
    job.setJobLink(request.getJobLink().orElse(null));
    job.setSalary(request.getSalary().orElse(null));
    job.setContactPersonName(request.getContactPersonName().orElse(null));
    job.setContactPersonEmail(request.getContactPersonEmail().orElse(null));
    job.setSkills(request.getSkills().orElse(null));

    job.setResume(linkedResume);

    return job;
  }

  public JobApplication toEntity(
      final CreateJobApplicationFromIndeedRequest request, final Resume linkedResume) {
    if (request == null) return null;

    final JobApplication job = new JobApplication();

    job.setCompany(request.getCompany());
    job.setDescription(request.getDescription());
    job.setNotes(request.getNotes());
    job.setJobType(request.getJobType());
    job.setRole(request.getRole());
    job.setLocation(request.getLocation());
    job.setJobLink(request.getJobLink());
    job.setSalary(request.getSalary());
    job.setContactPersonName(request.getContactPersonName());
    job.setContactPersonEmail(request.getContactPersonEmail());
    job.setSkills(request.getSkills());
    job.setStatus(Status.APPLIED);
    job.setEnrichmentStatus(EnrichmentStatus.PENDING_ENRICHMENT);
    job.setResume(linkedResume);

    return job;
  }

  public List<JobApplicationDto> toDtoList(
      final List<JobApplication> entities, final UserPreference userPreference) {
    if (entities == null) return List.of();
    return entities.stream()
        .filter(Objects::nonNull)
        .map(jobApplication -> this.toDto(jobApplication, userPreference))
        .toList();
  }

  public List<JobApplicationSummaryDto> toSummaryList(
      final List<JobApplication> entities, final UserPreference userPreference) {
    if (entities == null) return List.of();
    return entities.stream()
        .filter(Objects::nonNull)
        .map(jobApplication -> this.toSummaryDto(jobApplication, userPreference))
        .toList();
  }

  private String normalizeIfPending(final String value, final EnrichmentStatus enrichmentStatus) {
    if (enrichmentStatus == EnrichmentStatus.PENDING_ENRICHMENT
        && (value == null || value.isBlank())) {
      return null;
    }
    return value;
  }
}

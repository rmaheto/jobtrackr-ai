package com.codemaniac.jobtrackrai.mapper;

import com.codemaniac.jobtrackrai.dto.*;
import com.codemaniac.jobtrackrai.entity.JobApplication;
import com.codemaniac.jobtrackrai.entity.Resume;
import com.codemaniac.jobtrackrai.entity.UserPreference;
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

    return JobApplicationDto.builder()
        .id(entity.getId())
        .company(entity.getCompany())
        .role(entity.getRole())
        .location(entity.getLocation())
        .description(entity.getDescription())
        .notes(entity.getNotes())
        .jobType(entity.getJobType())
        .status(entity.getStatus().name())
        .salary(entity.getSalary())
        .skills(entity.getSkills())
        .contactPersonName(entity.getContactPersonName())
        .contactPersonEmail(entity.getContactPersonEmail())
        .jobLink(entity.getJobLink())
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
}

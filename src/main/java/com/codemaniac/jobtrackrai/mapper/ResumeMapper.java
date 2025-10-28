package com.codemaniac.jobtrackrai.mapper;

import com.codemaniac.jobtrackrai.dto.*;
import com.codemaniac.jobtrackrai.entity.*;
import com.codemaniac.jobtrackrai.enums.ResumeFileType;
import com.codemaniac.jobtrackrai.factory.DateRepresentationFactory;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResumeMapper {

  private final DateRepresentationFactory dateFactory;
  private final JobApplicationMapper jobAppMapper;

  public ResumeDto toDto(final Resume entity, final UserPreference userPreference) {
    if (entity == null) return null;

    return ResumeDto.builder()
        .id(entity.getId())
        .originalName(entity.getOriginalName())
        .fileType(entity.getFileType().name())
        .size(entity.getSize())
        .uploadDate(
            entity.getAudit() != null && entity.getAudit().getCreateTimestamp() != null
                ? dateFactory.create(
                    entity.getAudit().getCreateTimestamp().toInstant(ZoneOffset.UTC),
                    userPreference)
                : null)
        .linkedApplications(
            entity.getJobApplications() != null ? entity.getJobApplications().size() : 0)
        .userId(entity.getUser() != null ? entity.getUser().getId() : null)
        .previewUrl(entity.getS3Key())
        .jobApplications(mapJobApplications(entity.getJobApplications(), userPreference))
        .build();
  }

  private List<JobApplicationSummaryDto> mapJobApplications(
      final List<JobApplication> apps, final UserPreference userPreference) {
    if (apps == null) return List.of();
    return apps.stream()
        .filter(Objects::nonNull)
        .map(jb -> jobAppMapper.toSummaryDto(jb, userPreference))
        .toList();
  }

  public Resume toEntity(final ResumeDto dto, final User user) {
    if (dto == null) return null;

    final Resume resume = new Resume();
    resume.setId(dto.getId());
    resume.setOriginalName(dto.getOriginalName());
    resume.setFileType(parseFileType(dto.getFileType()));
    resume.setSize(dto.getSize());
    resume.setS3Key(dto.getPreviewUrl());
    resume.setUser(user);
    return resume;
  }

  private ResumeFileType parseFileType(final String fileType) {
    if (fileType == null) return null;
    try {
      return ResumeFileType.valueOf(fileType.toUpperCase());
    } catch (final IllegalArgumentException ignored) {
      // not a plain enum name, try MIME type next
    }

    // Handle MIME type (PDF, DOCX)
    try {
      return ResumeFileType.fromMimeType(fileType);
    } catch (final IllegalArgumentException ex) {
      throw new IllegalArgumentException("Unsupported file type: " + fileType, ex);
    }
  }
}

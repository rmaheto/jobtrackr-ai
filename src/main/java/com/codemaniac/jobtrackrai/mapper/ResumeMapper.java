package com.codemaniac.jobtrackrai.mapper;

import com.codemaniac.jobtrackrai.dto.JobApplicationSummaryDto;
import com.codemaniac.jobtrackrai.dto.ResumeDto;
import com.codemaniac.jobtrackrai.entity.JobApplication;
import com.codemaniac.jobtrackrai.entity.Resume;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ResumeMapper {

  @Mapping(source = "audit.createTimestamp", target = "uploadDate")
  @Mapping(source = "user.id", target = "userId")
  ResumeDto toDto(Resume resume);

  @Mapping(target = "audit.createTimestamp", source = "uploadDate")
  @Mapping(target = "user", ignore = true)
  Resume toEntity(ResumeDto dto);

  @Mapping(source = "id", target = "id")
  @Mapping(source = "company", target = "company")
  @Mapping(source = "role", target = "role")
  @Mapping(source = "status", target = "status")
  @Mapping(source = "appliedDate", target = "appliedDate")
  JobApplicationSummaryDto toJobApplicationSummaryDto(JobApplication jobApplication);

  @AfterMapping
  default void mapJobApplications(final Resume resume, @MappingTarget final ResumeDto dto) {
    if (resume.getJobApplications() != null) {
      dto.setJobApplications(
          resume.getJobApplications().stream().map(this::toJobApplicationSummaryDto).toList());
    }
  }
}

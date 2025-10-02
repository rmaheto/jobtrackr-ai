package com.codemaniac.jobtrackrai.mapper;

import com.codemaniac.jobtrackrai.dto.JobApplicationDto;
import com.codemaniac.jobtrackrai.dto.JobApplicationRequest;
import com.codemaniac.jobtrackrai.entity.JobApplication;
import java.util.List;
import java.util.Optional;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface JobApplicationMapper {

  JobApplication toEntity(JobApplicationRequest request);

  JobApplicationDto toDto(JobApplication entity);

  List<JobApplicationDto> toDtoList(List<JobApplication> entities);

  default String map(final Optional<String> value) {
    return value.orElse(null);
  }


  default Optional<String> map(final String value) {
    return Optional.ofNullable(value);
  }
}

package com.codemaniac.jobtrackrai.mapper;

import com.codemaniac.jobtrackrai.dto.FollowUpDto;
import com.codemaniac.jobtrackrai.entity.FollowUp;
import java.time.OffsetDateTime;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FollowUpMapper {

  @Mapping(source = "jobApplication.id", target = "applicationId")
  @Mapping(source = "jobApplication.role", target = "applicationTitle")
  @Mapping(source = "jobApplication.company", target = "company")
  FollowUpDto toDto(FollowUp followUp);

  // Helpers for MapStruct
  default String map(final OffsetDateTime value) {
    return value != null ? value.toString() : null;
  }

  default OffsetDateTime map(final String value) {
    return value != null ? OffsetDateTime.parse(value) : null;
  }

}

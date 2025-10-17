package com.codemaniac.jobtrackrai.mapper;

import com.codemaniac.jobtrackrai.dto.UserPreferenceDto;
import com.codemaniac.jobtrackrai.entity.UserPreference;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserPreferenceMapper {

  @Mapping(
      target = "theme",
      expression = "java(entity.getTheme() != null ? entity.getTheme().name() : null)")
  @Mapping(
      target = "landingPage",
      expression = "java(entity.getLandingPage() != null ? entity.getLandingPage().name() : null)")
  @Mapping(target = "defaultAppStatus", source = "defaultAppStatus")
  @Mapping(
      target = "followUpReminder",
      expression =
          "java(entity.getFollowUpReminder() != null ? entity.getFollowUpReminder().name() : null)")
  @Mapping(
      target = "exportFormat",
      expression =
          "java(entity.getExportFormat() != null ? entity.getExportFormat().name() : null)")
  @Mapping(
      target = "lastSaved",
      expression =
          "java(entity.getAudit() != null ? "
              + "(entity.getAudit().getUpdateTimestamp() != null ? "
              + "entity.getAudit().getUpdateTimestamp() : entity.getAudit().getCreateTimestamp()) : null)")
  UserPreferenceDto toDto(UserPreference entity);

  @Mapping(
      target = "theme",
      expression =
          "java(dto.getTheme() != null ? com.codemaniac.jobtrackrai.enums.Theme.valueOf(dto.getTheme()) : null)")
  @Mapping(
      target = "landingPage",
      expression =
          "java(dto.getLandingPage() != null ? com.codemaniac.jobtrackrai.enums.LandingPage.valueOf(dto.getLandingPage()) : null)")
  @Mapping(target = "defaultAppStatus", source = "defaultAppStatus")
  @Mapping(
      target = "followUpReminder",
      expression =
          "java(dto.getFollowUpReminder() != null ? com.codemaniac.jobtrackrai.enums.FollowUpReminder.valueOf(dto.getFollowUpReminder()) : null)")
  @Mapping(
      target = "exportFormat",
      expression =
          "java(dto.getExportFormat() != null ? com.codemaniac.jobtrackrai.enums.ExportFormat.valueOf(dto.getExportFormat()) : null)")
  UserPreference toEntity(UserPreferenceDto dto);
}

package com.codemaniac.jobtrackrai.service;

import com.codemaniac.jobtrackrai.dto.UserPreferenceDto;
import com.codemaniac.jobtrackrai.entity.User;
import com.codemaniac.jobtrackrai.entity.UserPreference;
import com.codemaniac.jobtrackrai.exception.InternalServerException;
import com.codemaniac.jobtrackrai.exception.NotFoundException;
import com.codemaniac.jobtrackrai.mapper.UserPreferenceMapper;
import com.codemaniac.jobtrackrai.repository.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserPreferenceServiceImpl implements UserPreferenceService {

  private final CurrentUserService currentUserService;
  private final UserPreferenceRepository preferenceRepository;
  private final UserPreferenceMapper mapper;

  @Override
  @Transactional(readOnly = true)
  public UserPreferenceDto getUserPreferences() {

    final User user = currentUserService.getCurrentUser();
    return preferenceRepository
        .findByUserId(user.getId())
        .map(mapper::toDto)
        .orElseThrow(
            () -> new NotFoundException("User preferences not found for ID: " + user.getId()));
  }

  @Override
  @Transactional
  public UserPreferenceDto updateUserPreferences(final UserPreferenceDto dto) {

    final User user = currentUserService.getCurrentUser();

    final UserPreference existingPref =
        preferenceRepository
            .findByUserId(user.getId())
            .orElse(UserPreference.builder().user(user).build());

    try {
      final UserPreference updatedEntity = mapper.toEntity(dto);
      updatedEntity.setId(existingPref.getId());
      updatedEntity.setUser(user);
      final UserPreference saved = preferenceRepository.saveAndFlush(updatedEntity);

      return mapper.toDto(saved);

    } catch (final IllegalArgumentException e) {
      log.error(
          "Invalid enum value provided while updating preferences for user {}: {}",
          user.getId(),
          e.getMessage());
      throw new IllegalArgumentException("Invalid preference value. Please check your selections.");
    } catch (final Exception e) {
      log.error(
          "Unexpected error updating preferences for user {}: {}", user.getId(), e.getMessage());
      throw new InternalServerException("Could not update preferences. Please try again later.");
    }
  }
}

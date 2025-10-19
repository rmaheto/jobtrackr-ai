package com.codemaniac.jobtrackrai.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.codemaniac.jobtrackrai.dto.UserPreferenceDto;
import com.codemaniac.jobtrackrai.entity.User;
import com.codemaniac.jobtrackrai.entity.UserPreference;
import com.codemaniac.jobtrackrai.exception.InternalServerException;
import com.codemaniac.jobtrackrai.exception.NotFoundException;
import com.codemaniac.jobtrackrai.mapper.UserPreferenceMapper;
import com.codemaniac.jobtrackrai.repository.UserPreferenceRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserPreferenceServiceImplTest {

  @Mock private CurrentUserService currentUserService;

  @Mock private UserPreferenceRepository preferenceRepository;

  @Mock private UserPreferenceMapper mapper;

  @InjectMocks private UserPreferenceServiceImpl service;

  private User user;
  private UserPreference entity;
  private UserPreferenceDto dto;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(1L);

    entity = new UserPreference();
    entity.setId(10L);
    entity.setUser(user);

    dto = new UserPreferenceDto();
  }

  @Test
  void getUserPreferences_Dto_whenExistingPreference_shouldReturnMappedDto() {
    when(currentUserService.getCurrentUser()).thenReturn(user);
    when(preferenceRepository.findByUserId(user.getId())).thenReturn(Optional.of(entity));
    when(mapper.toDto(entity)).thenReturn(dto);

    final UserPreferenceDto result = service.getUserPreferencesDto();

    assertNotNull(result);
    verify(preferenceRepository).findByUserId(user.getId());
    verify(mapper).toDto(entity);
  }

  @Test
  void getUserPreferences_Dto_whenNotFound_shouldThrowNotFoundException() {
    when(currentUserService.getCurrentUser()).thenReturn(user);
    when(preferenceRepository.findByUserId(user.getId())).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> service.getUserPreferences());

    verify(preferenceRepository).findByUserId(user.getId());
    verify(mapper, never()).toDto(any());
  }

  @Test
  void updateUserPreferences_whenExistingPreference_shouldUpdateAndReturnDto() {
    when(currentUserService.getCurrentUser()).thenReturn(user);
    when(preferenceRepository.findByUserId(user.getId())).thenReturn(Optional.of(entity));
    when(mapper.toEntity(dto)).thenReturn(entity);
    when(preferenceRepository.saveAndFlush(entity)).thenReturn(entity);
    when(mapper.toDto(entity)).thenReturn(dto);

    final UserPreferenceDto result = service.updateUserPreferences(dto);

    assertNotNull(result);
    verify(preferenceRepository).saveAndFlush(entity);
    verify(mapper).toDto(entity);
  }

  @Test
  void updateUserPreferences_whenNoExistingPreference_shouldCreateNewAndReturnDto() {
    when(currentUserService.getCurrentUser()).thenReturn(user);
    when(preferenceRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
    when(mapper.toEntity(dto)).thenReturn(entity);
    when(preferenceRepository.saveAndFlush(any(UserPreference.class))).thenReturn(entity);
    when(mapper.toDto(entity)).thenReturn(dto);

    final UserPreferenceDto result = service.updateUserPreferences(dto);

    assertNotNull(result);
    verify(preferenceRepository).saveAndFlush(any(UserPreference.class));
  }

  @Test
  void updateUserPreferences_whenMapperThrowsIllegalArgument_shouldThrowIllegalArgumentException() {
    when(currentUserService.getCurrentUser()).thenReturn(user);
    when(preferenceRepository.findByUserId(user.getId())).thenReturn(Optional.of(entity));
    when(mapper.toEntity(dto)).thenThrow(new IllegalArgumentException("Invalid enum value"));

    assertThrows(IllegalArgumentException.class, () -> service.updateUserPreferences(dto));

    verify(preferenceRepository, never()).saveAndFlush(any());
  }

  @Test
  void updateUserPreferences_whenUnexpectedError_shouldThrowInternalServerException() {
    when(currentUserService.getCurrentUser()).thenReturn(user);
    when(preferenceRepository.findByUserId(user.getId())).thenReturn(Optional.of(entity));
    when(mapper.toEntity(dto)).thenReturn(entity);
    when(preferenceRepository.saveAndFlush(any(UserPreference.class)))
        .thenThrow(new RuntimeException("Database error"));

    assertThrows(InternalServerException.class, () -> service.updateUserPreferences(dto));

    verify(preferenceRepository).saveAndFlush(any(UserPreference.class));
  }
}

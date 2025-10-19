package com.codemaniac.jobtrackrai.service;

import com.codemaniac.jobtrackrai.dto.UserPreferenceDto;
import com.codemaniac.jobtrackrai.entity.UserPreference;

public interface UserPreferenceService {

  UserPreference getUserPreferences();

  UserPreferenceDto getUserPreferencesDto();

  UserPreferenceDto updateUserPreferences(UserPreferenceDto dto);
}

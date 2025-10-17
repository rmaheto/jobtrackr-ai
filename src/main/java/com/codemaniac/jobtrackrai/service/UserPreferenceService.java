package com.codemaniac.jobtrackrai.service;

import com.codemaniac.jobtrackrai.dto.UserPreferenceDto;

public interface UserPreferenceService {
  UserPreferenceDto getUserPreferences();

  UserPreferenceDto updateUserPreferences(UserPreferenceDto dto);
}

package com.codemaniac.jobtrackrai.controller;

import com.codemaniac.jobtrackrai.dto.UserPreferenceDto;
import com.codemaniac.jobtrackrai.model.ApiResponse;
import com.codemaniac.jobtrackrai.service.UserPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/me/preferences")
@RequiredArgsConstructor
public class UserPreferenceController {

  private final UserPreferenceService preferenceService;

  @GetMapping
  public ResponseEntity<ApiResponse<UserPreferenceDto>> getPreferences() {
    final var dto = preferenceService.getUserPreferences();
    return ResponseEntity.ok(ApiResponse.of("OK", "User preferences retrieved", dto));
  }

  @PutMapping
  public ResponseEntity<ApiResponse<UserPreferenceDto>> updatePreferences(
      @RequestBody final UserPreferenceDto dto) {
    final var updated = preferenceService.updateUserPreferences(dto);
    return ResponseEntity.ok(ApiResponse.of("OK", "Preferences updated successfully", updated));
  }
}

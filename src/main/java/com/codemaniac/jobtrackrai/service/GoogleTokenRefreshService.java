package com.codemaniac.jobtrackrai.service;

import com.codemaniac.jobtrackrai.entity.User;
import com.codemaniac.jobtrackrai.repository.UserRepository;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleTokenRefreshService {

  @Value("${spring.security.oauth2.client.registration.google.client-id}")
  private String clientId;

  @Value("${spring.security.oauth2.client.registration.google.client-secret}")
  private String clientSecret;

  @Value(("${google.refreshTokenUrl}"))
  private String refreshTokenUrl;

  private final UserRepository userRepository;
  private final RestTemplate restTemplate;

  public void refreshAccessToken(final User user) {
    if (user.getGoogleRefreshToken() == null) {
      log.warn("User {} has no refresh token", user.getEmail());
      return;
    }

    if (user.getTokenExpiry() != null
        && Instant.now().isBefore(Instant.ofEpochMilli(user.getTokenExpiry()))) {
      // Token still valid
      return;
    }

    log.info("Refreshing Google access token for {}", user.getEmail());

    try {

      final HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

      final String body =
          "client_id="
              + clientId
              + "&client_secret="
              + clientSecret
              + "&refresh_token="
              + user.getGoogleRefreshToken()
              + "&grant_type=refresh_token";

      final HttpEntity<String> entity = new HttpEntity<>(body, headers);

      final ResponseEntity<Map<String, Object>> response =
          restTemplate.exchange(
              refreshTokenUrl, HttpMethod.POST, entity, new ParameterizedTypeReference<>() {});

      final Map<String, Object> map = response.getBody();
      if (response.getStatusCode().is2xxSuccessful() && map != null) {
        final String newAccessToken = (String) map.get("access_token");
        final Number expiresIn = (Number) map.get("expires_in");

        if (newAccessToken != null) {
          user.setGoogleAccessToken(newAccessToken);
          if (expiresIn != null) {
            user.setTokenExpiry(Instant.now().plusSeconds(expiresIn.longValue()).toEpochMilli());
          }
          userRepository.save(user);
          log.info("Access token refreshed successfully for {}", user.getEmail());
          return;
        }
      }

      log.warn("Failed to refresh token for {}: {}", user.getEmail(), response.getBody());
    } catch (final Exception e) {
      log.error("Error refreshing token for {}: {}", user.getEmail(), e.getMessage());
    }
  }
}

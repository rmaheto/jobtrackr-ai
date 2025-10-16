package com.codemaniac.jobtrackrai.service;

import java.util.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.userinfo.*;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.core.user.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@AllArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

  private final RestTemplate restTemplate;

  private static final String EMAIL = "email";
  private static final String PICTURE = "picture";
  private static final String PICTURE_URL = "pictureUrl";

  @Override
  public OAuth2User loadUser(final OAuth2UserRequest userRequest)
      throws OAuth2AuthenticationException {
    final OAuth2User oAuth2User = super.loadUser(userRequest);
    final Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());

    final String registrationId =
        userRequest.getClientRegistration().getRegistrationId().toLowerCase();
    attributes.put("provider", registrationId);

    switch (registrationId) {
      case "github" -> handleGitHub(userRequest, attributes);
      case "google" -> handleGoogle(attributes);
      case "facebook" -> handleFacebook(attributes);
      case "linkedin" -> handleLinkedIn(attributes);
      default -> log.warn("Unknown provider: {}", registrationId);
    }

    final String userNameAttributeName =
        userRequest
            .getClientRegistration()
            .getProviderDetails()
            .getUserInfoEndpoint()
            .getUserNameAttributeName();

    log.info("Loaded user from {} with key attribute {}", registrationId, userNameAttributeName);

    return new DefaultOAuth2User(oAuth2User.getAuthorities(), attributes, userNameAttributeName);
  }

  private void handleGitHub(
      final OAuth2UserRequest userRequest, final Map<String, Object> attributes) {
    if (attributes.get(EMAIL) == null) {
      try {
        final String email = fetchGitHubPrimaryEmail(userRequest);
        if (email != null) attributes.put(EMAIL, email);
      } catch (final Exception e) {
        log.warn("Failed to fetch GitHub email: {}", e.getMessage());
      }
    }
    attributes.put("name", attributes.getOrDefault("name", attributes.get("login")));
    attributes.put(PICTURE_URL, attributes.get("avatar_url"));
  }

  private void handleGoogle(final Map<String, Object> attributes) {
    attributes.putIfAbsent(PICTURE_URL, attributes.get(PICTURE));
  }

  private void handleFacebook(final Map<String, Object> attributes) {
    if (attributes.get(PICTURE) instanceof final Map<?, ?> pictureObj) {
      final Map<?, ?> data = (Map<?, ?>) pictureObj.get("data");
      if (data != null && data.get("url") != null) {
        attributes.put(PICTURE_URL, data.get("url"));
      }
    }
  }

  private void handleLinkedIn(final Map<String, Object> attributes) {
    final Object first = attributes.get("localizedFirstName");
    final Object last = attributes.get("localizedLastName");
    if (first != null && last != null) {
      attributes.put("name", first + " " + last);
    }

    final Object profilePicObj = attributes.get("profilePicture");
    if (!(profilePicObj instanceof final Map<?, ?> profilePic)) {
      return;
    }

    try {
      final Object displayImageObj = profilePic.get("displayImage~");
      if (!(displayImageObj instanceof final Map<?, ?> displayImage)) {
        return;
      }

      final Object elementsObj = displayImage.get("elements");
      if (!(elementsObj instanceof final List<?> elements) || elements.isEmpty()) {
        return;
      }

      final Object firstElObj = elements.get(0);
      if (!(firstElObj instanceof final Map<?, ?> firstEl)) {
        return;
      }

      final Object identifiersObj = firstEl.get("identifiers");
      if (!(identifiersObj instanceof final List<?> identifiers) || identifiers.isEmpty()) {
        return;
      }

      final Object identifierObj = identifiers.get(0);
      if (identifierObj instanceof final Map<?, ?> identifierMap) {
        final Object url = identifierMap.get("identifier");
        if (url instanceof final String urlStr) {
          attributes.put(PICTURE_URL, urlStr);
        }
      }
    } catch (final Exception ex) {
      log.warn("Failed to parse LinkedIn picture", ex);
    }
  }

  private String fetchGitHubPrimaryEmail(final OAuth2UserRequest userRequest) {
    final String emailEndpoint = "https://api.github.com/user/emails";

    final HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.AUTHORIZATION, "token " + userRequest.getAccessToken().getTokenValue());
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

    final HttpEntity<Void> entity = new HttpEntity<>(headers);

    final ResponseEntity<List<Map<String, Object>>> response =
        restTemplate.exchange(
            emailEndpoint, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {});

    final List<Map<String, Object>> emails = response.getBody();
    if (emails == null || emails.isEmpty()) return null;

    return emails.stream()
        .filter(e -> Boolean.TRUE.equals(e.get("primary")))
        .map(e -> (String) e.get(EMAIL))
        .findFirst()
        .orElse(null);
  }
}

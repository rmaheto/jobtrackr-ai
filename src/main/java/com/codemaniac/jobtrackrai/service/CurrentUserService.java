package com.codemaniac.jobtrackrai.service;

import com.codemaniac.jobtrackrai.entity.User;
import com.codemaniac.jobtrackrai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CurrentUserService {

  private final UserRepository userRepository;

  /** Retrieve the currently authenticated user based on JWT. */
  public User getCurrentUser() {
    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (!(authentication instanceof final JwtAuthenticationToken jwtAuth)) {
      log.error("No JWT authentication found");
      throw new IllegalStateException("No JWT authentication found");
    }

    final Jwt jwt = jwtAuth.getToken();
    final String externalId = jwt.getSubject();
    final String email = authentication.getName();
    final String name = jwt.getClaimAsString("name");
    final String pictureUrl = jwt.getClaimAsString("pictureUrl");

    return userRepository
        .findByEmail(email)
        .orElseGet(
            () ->
                userRepository.save(
                    User.builder()
                        .externalId(externalId)
                        .email(email)
                        .name(name)
                        .pictureUrl(pictureUrl)
                        .build()));
  }

  /**
   * Creates or updates a user record when authenticated via OAuth2 login. All provider-specific
   * normalization is done by CustomOAuth2UserService.
   */
  public User getOrCreateFromOAuth2(final OAuth2User oAuth2User) {
    final String externalId =
        String.valueOf(
            oAuth2User.getAttribute("sub") != null
                ? oAuth2User.getAttribute("sub")
                : oAuth2User.getName());

    final String email = oAuth2User.getAttribute("email");
    final String name = oAuth2User.getAttribute("name");
    final String pictureUrl = oAuth2User.getAttribute("pictureUrl");
    final String provider = oAuth2User.getAttribute("provider");

    log.debug("OAuth2 login -> extId={} email={} provider={}", externalId, email, provider);

    return userRepository
        .findByEmail(email)
        .map(existing -> updateIfChanged(existing, name, pictureUrl, provider))
        .orElseGet(
            () -> {
              log.info("Provisioning new user: {}", email);
              return userRepository.save(
                  User.builder()
                      .externalId(externalId)
                      .email(email)
                      .name(name)
                      .pictureUrl(pictureUrl)
                      .provider(provider)
                      .build());
            });
  }

  public User save(final User user) {
    return userRepository.save(user);
  }

  private User updateIfChanged(
      final User existing, final String name, final String pictureUrl, final String provider) {
    boolean updated = false;

    if (name != null && !name.equals(existing.getName())) {
      existing.setName(name);
      updated = true;
    }

    if (pictureUrl != null && !pictureUrl.equals(existing.getPictureUrl())) {
      existing.setPictureUrl(pictureUrl);
      updated = true;
    }

    if (provider != null && !provider.equals(existing.getProvider())) {
      existing.setProvider(provider);
      updated = true;
    }

    return updated ? userRepository.save(existing) : existing;
  }
}

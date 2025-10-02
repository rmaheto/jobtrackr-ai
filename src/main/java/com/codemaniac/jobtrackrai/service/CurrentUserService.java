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

  /**
   * Returns the managed User entity for the currently authenticated user. If the user logs in for
   * the first time, auto-provisions them.
   */
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

    return userRepository
        .findByExternalId(externalId)
        .orElseGet(
            () ->
                userRepository.save(
                    User.builder().externalId(externalId).email(email).name(name).build()));
  }

  /**
   * Handles provisioning when the principal is an OAuth2User (e.g., directly after OAuth2 login
   * success).
   */
  public User getOrCreateFromOAuth2(final OAuth2User oAuth2User) {
    final String extId =
        oAuth2User.getAttribute("sub") != null
            ? oAuth2User.getAttribute("sub")
            : oAuth2User.getName();

    final String emailAddr = oAuth2User.getAttribute("email");
    final String fullName = oAuth2User.getAttribute("name");

    log.debug("Resolving user from OAuth2 login externalId={} email={}", extId, emailAddr);

    return userRepository
        .findByExternalId(extId)
        .orElseGet(
            () -> {
              log.info("Provisioning new user via OAuth2 externalId={} email={}", extId, emailAddr);
              return userRepository.save(
                  User.builder().externalId(extId).email(emailAddr).name(fullName).build());
            });
  }
}

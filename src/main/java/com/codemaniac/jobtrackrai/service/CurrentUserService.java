package com.codemaniac.jobtrackrai.service;

import com.codemaniac.jobtrackrai.entity.User;
import com.codemaniac.jobtrackrai.entity.UserPreference;
import com.codemaniac.jobtrackrai.enums.ExportFormat;
import com.codemaniac.jobtrackrai.enums.FollowUpReminder;
import com.codemaniac.jobtrackrai.enums.LandingPage;
import com.codemaniac.jobtrackrai.enums.Status;
import com.codemaniac.jobtrackrai.enums.Theme;
import com.codemaniac.jobtrackrai.repository.UserPreferenceRepository;
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
  private final UserPreferenceRepository preferenceRepository;

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

    final User user =
        userRepository
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

    preferenceRepository
        .findByUserId(user.getId())
        .orElseGet(() -> preferenceRepository.save(createDefaultsFor(user)));

    return user;
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
              final User newUser =
                  userRepository.save(
                      User.builder()
                          .externalId(externalId)
                          .email(email)
                          .name(name)
                          .pictureUrl(pictureUrl)
                          .provider(provider)
                          .build());
              createDefaultPreferences(newUser);
              return newUser;
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

  private void createDefaultPreferences(final User user) {
    try {
      if (preferenceRepository.findByUserId(user.getId()).isEmpty()) {
        preferenceRepository.save(createDefaultsFor(user));
        log.info("Default preferences created for new user: {}", user.getEmail());
      }
    } catch (Exception e) {
      log.error(
          "Failed to create default preferences for user {}: {}", user.getEmail(), e.getMessage());
    }
  }

  private UserPreference createDefaultsFor(final User user) {
    return UserPreference.builder()
        .user(user)
        .theme(Theme.LIGHT)
        .language("English")
        .timezone("Eastern Time (UTC-5)")
        .dateFormat("MM/DD/YYYY (US)")
        .profileVisibility("Public")
        .showContactInfo(true)
        .allowSearchIndexing(true)
        .showActivityStatus(false)
        .emailNotifications(true)
        .pushNotifications(true)
        .smsNotifications(false)
        .statusUpdates(true)
        .interviewReminders(true)
        .weeklyDigest(true)
        .marketingEmails(false)
        .landingPage(LandingPage.DASHBOARD)
        .itemsPerPage(10)
        .compactView(false)
        .showQuickStats(true)
        .defaultAppStatus(Status.APPLIED)
        .followUpReminder(FollowUpReminder.ONE_WEEK)
        .exportFormat(ExportFormat.PDF)
        .autoSaveDrafts(true)
        .build();
  }
}

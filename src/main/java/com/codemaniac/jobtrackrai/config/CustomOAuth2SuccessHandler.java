package com.codemaniac.jobtrackrai.config;

import com.codemaniac.jobtrackrai.entity.User;
import com.codemaniac.jobtrackrai.service.CurrentUserService;
import com.codemaniac.jobtrackrai.service.JwtTokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

  private final JwtTokenService jwtTokenService;
  private final CurrentUserService currentUserService;
  private final OAuth2AuthorizedClientService authorizedClientService;

  @Value("${app.frontend.url}")
  private String redirectBaseUrl;

  @Override
  public void onAuthenticationSuccess(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final Authentication authentication)
      throws IOException, ServletException {

    final OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
    final User user = currentUserService.getOrCreateFromOAuth2(oAuth2User);

    if (authentication instanceof final OAuth2AuthenticationToken authToken) {
      final OAuth2AuthorizedClient client =
          authorizedClientService.loadAuthorizedClient(
              authToken.getAuthorizedClientRegistrationId(), authToken.getName());

      if (client != null) {
        final OAuth2AccessToken accessToken = client.getAccessToken();
        final OAuth2RefreshToken refreshToken = client.getRefreshToken();

        if (accessToken != null) {
          user.setGoogleAccessToken(accessToken.getTokenValue());
          user.setTokenExpiry(
              accessToken.getExpiresAt() != null
                  ? accessToken.getExpiresAt().toEpochMilli()
                  : null);
        }
        if (refreshToken != null) {
          user.setGoogleRefreshToken(refreshToken.getTokenValue());
        }
      }
    }

    currentUserService.save(user);

    final String token = jwtTokenService.generateToken(user);
    log.debug("User {} authenticated via {} and issued JWT", user.getEmail(), user.getProvider());

    final String redirectUrl = redirectBaseUrl + "/oauth/callback?token=" + token;
    response.sendRedirect(redirectUrl);
  }
}

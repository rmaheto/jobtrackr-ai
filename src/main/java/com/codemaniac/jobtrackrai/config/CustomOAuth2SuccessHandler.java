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
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

  private final JwtTokenService jwtTokenService;
  private final CurrentUserService currentUserService;

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

    final String token = jwtTokenService.generateToken(user);

    log.info(
        "User {} authenticated via {} and issued JWT",
        user.getEmail(),
        authentication.getAuthorities());

    final String redirectUrl = redirectBaseUrl + "/oauth/callback?token=" + token;
    response.sendRedirect(redirectUrl);
  }
}

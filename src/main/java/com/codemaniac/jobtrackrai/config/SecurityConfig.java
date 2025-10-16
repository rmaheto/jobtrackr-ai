package com.codemaniac.jobtrackrai.config;

import com.codemaniac.jobtrackrai.service.CustomOAuth2UserService;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration
@Slf4j
@RequiredArgsConstructor
public class SecurityConfig {

  @Value("${jwt.secret}")
  private String secret;

  @Value("${app.frontend.url}")
  private String frontendBaseUrl;

  private final JwtAuthConverter jwtAuthConverter;
  private final CustomOAuth2SuccessHandler successHandler;
  private final CustomCorsConfiguration customCorsConfiguration;
  private final CustomOAuth2UserService customOAuth2UserService;

  @Bean
  public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(customCorsConfiguration))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/actuator/health", "/actuator/info", "/api/auth/logout")
                    .permitAll()
                    .requestMatchers("/api/**")
                    .authenticated()
                    .anyRequest()
                    .permitAll())
        .oauth2Login(
            oauth2 ->
                oauth2
                    .failureHandler(
                        (request, response, exception) -> {
                          log.warn("OAuth2 login failed: {}", exception.getMessage());
                          final String redirectUrl = frontendBaseUrl + "/login?error=access_denied";
                          response.sendRedirect(redirectUrl);
                        })
                    .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                    .successHandler(successHandler))
        .oauth2ResourceServer(
            oauth2 ->
                oauth2.jwt(
                    jwt -> jwt.decoder(jwtDecoder()).jwtAuthenticationConverter(jwtAuthConverter)));

    return http.build();
  }

  @Bean
  public SecretKey jwtSecretKey() {
    return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  @Bean
  public JwtDecoder jwtDecoder() {
    return NimbusJwtDecoder.withSecretKey(jwtSecretKey()).build();
  }
}

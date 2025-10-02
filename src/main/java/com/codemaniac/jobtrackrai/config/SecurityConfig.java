package com.codemaniac.jobtrackrai.config;

import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class SecurityConfig {

  @Value("${jwt.secret}")
  private String secret;

  private final JwtAuthConverter jwtAuthConverter;

  private final CustomOAuth2SuccessHandler successHandler;

  private final CustomCorsConfiguration customCorsConfiguration;

  @Bean
  public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(customCorsConfiguration))
        .authorizeHttpRequests(
            auth -> auth.requestMatchers("/api/**").authenticated().anyRequest().permitAll())
        .oauth2Login(oauth2 -> oauth2.successHandler(successHandler))
        .oauth2ResourceServer(
            oauth2 ->
                oauth2.jwt(
                    jwt -> jwt.decoder(jwtDecoder()).jwtAuthenticationConverter(jwtAuthConverter)))
        .csrf(AbstractHttpConfigurer::disable);

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

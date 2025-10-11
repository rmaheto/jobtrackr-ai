package com.codemaniac.jobtrackrai.config;

import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

@Component
public class CustomCorsConfiguration implements CorsConfigurationSource {
  @Override
  public CorsConfiguration getCorsConfiguration(@Nonnull final HttpServletRequest request) {
    final CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(
        List.of(
            "http://localhost:3000",
            "http://127.0.0.1:8080",
            "https://jobtrackrpro.com",
            "https://www.jobtrackrpro.com",
            "https://api.jobtrackrpro.com"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);
    config.setExposedHeaders(List.of("Authorization", "Content-Type"));
    return config;
  }
}

package com.codemaniac.jobtrackrai.config;

import jakarta.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Configuration
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

  @Override
  public AbstractAuthenticationToken convert(@Nonnull final Jwt jwt) {
    final Collection<GrantedAuthority> authorities = extractAuthorities(jwt);

    String email = jwt.getClaimAsString("email");
    if (email == null) {
      email = jwt.getSubject();
    }

    return new JwtAuthenticationToken(jwt, authorities, email);
  }

  private Collection<GrantedAuthority> extractAuthorities(final Jwt jwt) {
    // you can map Google roles/groups here if needed
    return Collections.emptyList();
  }
}


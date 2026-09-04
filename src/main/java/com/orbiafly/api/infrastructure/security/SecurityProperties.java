package com.orbiafly.api.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(
    boolean enabled,
    Jwt jwt,
    InitialAdmin initialAdmin) {

  public record Jwt(String secret, String issuer, long expirationMinutes) {
  }

  public record InitialAdmin(String username, String email, String password) {
  }
}

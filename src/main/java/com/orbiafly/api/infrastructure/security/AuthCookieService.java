package com.orbiafly.api.infrastructure.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class AuthCookieService {

  public static final String COOKIE_NAME = "orbiafly_session";

  private final boolean secure;
  private final Duration maxAge;

  public AuthCookieService(
      @Value("${app.security.cookie-secure}") boolean secure,
      @Value("${app.security.jwt-expiration-minutes}") long expirationMinutes) {
    this.secure = secure;
    this.maxAge = Duration.ofMinutes(expirationMinutes);
  }

  public void write(HttpServletResponse response, String token) {
    response.addHeader(HttpHeaders.SET_COOKIE, cookie(token, maxAge).toString());
  }

  public void clear(HttpServletResponse response) {
    response.addHeader(HttpHeaders.SET_COOKIE, cookie("", Duration.ZERO).toString());
  }

  public Optional<String> read(HttpServletRequest request) {
    if (request.getCookies() == null) {
      return Optional.empty();
    }
    return Arrays.stream(request.getCookies())
        .filter(cookie -> COOKIE_NAME.equals(cookie.getName()))
        .map(Cookie::getValue)
        .filter(value -> !value.isBlank())
        .findFirst();
  }

  private ResponseCookie cookie(String value, Duration age) {
    return ResponseCookie.from(COOKIE_NAME, value)
        .httpOnly(true)
        .secure(secure)
        .sameSite("Strict")
        .path("/")
        .maxAge(age)
        .build();
  }
}

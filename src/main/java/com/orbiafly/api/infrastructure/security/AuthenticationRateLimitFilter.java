package com.orbiafly.api.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AuthenticationRateLimitFilter extends OncePerRequestFilter {

  private static final Set<String> PROTECTED_PATHS = Set.of(
      "/api/v1/auth/register",
      "/api/v1/auth/login",
      "/api/v1/auth/password-reset/request",
      "/api/v1/auth/password-reset/confirm");

  private final ConcurrentMap<String, Window> windows = new ConcurrentHashMap<>();
  private final Clock clock;
  private final int maxAttempts;
  private final long windowMillis;

  public AuthenticationRateLimitFilter(
      Clock clock,
      @Value("${app.security.rate-limit-attempts}") int maxAttempts,
      @Value("${app.security.rate-limit-window-seconds}") long windowSeconds) {
    this.clock = clock;
    this.maxAttempts = maxAttempts;
    this.windowMillis = windowSeconds * 1000;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !"POST".equals(request.getMethod()) || !PROTECTED_PATHS.contains(request.getRequestURI());
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    long now = clock.millis();
    String key = request.getRemoteAddr() + ':' + request.getRequestURI();
    Window window = windows.compute(key, (ignored, current) ->
        current == null || now - current.startedAt() >= windowMillis
            ? new Window(now, new AtomicInteger(1))
            : increment(current));
    if (window.attempts().get() > maxAttempts) {
      response.setStatus(429);
      response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
      response.getWriter().write("{\"title\":\"Muitas tentativas\",\"status\":429,\"detail\":\"Tente novamente mais tarde.\"}");
      return;
    }
    if (windows.size() > 10_000) {
      windows.entrySet().removeIf(entry -> now - entry.getValue().startedAt() >= windowMillis);
    }
    filterChain.doFilter(request, response);
  }

  private Window increment(Window window) {
    window.attempts().incrementAndGet();
    return window;
  }

  private record Window(long startedAt, AtomicInteger attempts) {
  }
}

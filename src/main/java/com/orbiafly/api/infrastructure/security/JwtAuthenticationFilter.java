package com.orbiafly.api.infrastructure.security;

import com.orbiafly.api.modules.auth.infrastructure.persistence.AppUserRepositoryInterface;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final AppUserRepositoryInterface userRepository;
  private final AuthCookieService cookieService;

  public JwtAuthenticationFilter(
      JwtService jwtService,
      AppUserRepositoryInterface userRepository,
      AuthCookieService cookieService) {
    this.jwtService = jwtService;
    this.userRepository = userRepository;
    this.cookieService = cookieService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    tokenFrom(request).ifPresent(this::authenticate);
    filterChain.doFilter(request, response);
  }

  private Optional<String> tokenFrom(HttpServletRequest request) {
    String header = request.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ")) {
      return Optional.of(header.substring(7));
    }
    return cookieService.read(request);
  }

  private void authenticate(String token) {
    try {
      JwtService.JwtClaims claims = jwtService.parseToken(token);
      userRepository.findById(claims.userId())
          .filter(user -> user.isActive() && user.getTokenVersion() == claims.tokenVersion())
          .map(AppUserPrincipal::new)
          .ifPresent(principal -> SecurityContextHolder.getContext().setAuthentication(
              new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())));
    } catch (RuntimeException exception) {
      SecurityContextHolder.clearContext();
    }
  }
}

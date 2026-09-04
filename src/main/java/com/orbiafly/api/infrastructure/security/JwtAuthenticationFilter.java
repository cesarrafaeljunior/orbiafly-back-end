package com.orbiafly.api.infrastructure.security;

import com.orbiafly.api.modules.auth.infrastructure.persistence.AppUserRepositoryInterface;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final AppUserRepositoryInterface userRepository;

  public JwtAuthenticationFilter(
      JwtService jwtService,
      AppUserRepositoryInterface userRepository) {
    this.jwtService = jwtService;
    this.userRepository = userRepository;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    String header = request.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ")) {
      authenticate(header.substring(7));
    }
    filterChain.doFilter(request, response);
  }

  private void authenticate(String token) {
    try {
      JwtService.JwtClaims claims = jwtService.parseToken(token);
      userRepository.findById(claims.userId())
          .filter(user -> user.isActive())
          .map(AppUserPrincipal::new)
          .ifPresent(principal -> SecurityContextHolder.getContext().setAuthentication(
              new UsernamePasswordAuthenticationToken(
                  principal,
                  null,
                  principal.getAuthorities())));
    } catch (RuntimeException exception) {
      SecurityContextHolder.clearContext();
    }
  }
}

package com.orbiafly.api.modules.auth.application.service;

import com.orbiafly.api.modules.auth.application.contract.CurrentUserServiceInterface;
import com.orbiafly.api.modules.fixedexpense.application.ResourceNotFoundException;
import com.orbiafly.api.modules.user.domain.User;
import com.orbiafly.api.modules.user.infrastructure.persistence.UserRepositoryInterface;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserServiceImpl implements CurrentUserServiceInterface {

  private final UserRepositoryInterface userRepository;

  public CurrentUserServiceImpl(UserRepositoryInterface userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new ResourceNotFoundException("Usuário autenticado não encontrado.");
    }
    Object principal = authentication.getPrincipal();
    if (principal instanceof Jwt jwt) {
      return findUser(UUID.fromString(jwt.getSubject()));
    }
    throw new ResourceNotFoundException("Usuário autenticado não encontrado.");
  }

  private User findUser(UUID id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Usuário autenticado não encontrado."));
  }
}

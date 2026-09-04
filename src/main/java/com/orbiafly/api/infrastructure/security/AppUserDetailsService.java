package com.orbiafly.api.infrastructure.security;

import com.orbiafly.api.modules.auth.infrastructure.persistence.AppUserRepositoryInterface;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {

  private final AppUserRepositoryInterface userRepository;

  public AppUserDetailsService(AppUserRepositoryInterface userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String login) {
    return userRepository.findByEmailIgnoreCase(login)
        .or(() -> userRepository.findByUsernameIgnoreCase(login))
        .map(AppUserPrincipal::new)
        .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));
  }
}

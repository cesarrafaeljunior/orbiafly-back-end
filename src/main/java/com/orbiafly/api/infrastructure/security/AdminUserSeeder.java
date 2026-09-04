package com.orbiafly.api.infrastructure.security;

import com.orbiafly.api.modules.auth.domain.AppRole;
import com.orbiafly.api.modules.auth.domain.AppUser;
import com.orbiafly.api.modules.auth.infrastructure.persistence.AppUserRepositoryInterface;
import java.time.Clock;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminUserSeeder implements CommandLineRunner {

  private final AppUserRepositoryInterface userRepository;
  private final PasswordEncoder passwordEncoder;
  private final Clock clock;
  private final String email;
  private final String username;
  private final String password;

  public AdminUserSeeder(
      AppUserRepositoryInterface userRepository,
      PasswordEncoder passwordEncoder,
      Clock clock,
      @Value("${app.admin.email}") String email,
      @Value("${app.admin.username}") String username,
      @Value("${app.admin.password}") String password) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.clock = clock;
    this.email = email;
    this.username = username;
    this.password = password;
  }

  @Override
  @Transactional
  public void run(String... args) {
    userRepository.findByEmailIgnoreCase(email)
        .ifPresentOrElse(
            user -> user.getRoles().add(AppRole.ROLE_ADMIN),
            () -> userRepository.save(new AppUser(
                email.toLowerCase(),
                username,
                passwordEncoder.encode(password),
                Set.of(AppRole.ROLE_USER, AppRole.ROLE_ADMIN),
                clock.instant())));
  }
}

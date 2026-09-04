package com.orbiafly.api.infrastructure.security;

import com.orbiafly.api.modules.user.domain.User;
import com.orbiafly.api.modules.user.domain.UserRole;
import com.orbiafly.api.modules.user.infrastructure.persistence.UserRepositoryInterface;
import java.time.Clock;
import java.time.Instant;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class InitialAdminUserInitializer implements ApplicationRunner {

  private final UserRepositoryInterface userRepository;
  private final PasswordEncoder passwordEncoder;
  private final SecurityProperties securityProperties;
  private final Clock clock;

  public InitialAdminUserInitializer(
      UserRepositoryInterface userRepository,
      PasswordEncoder passwordEncoder,
      SecurityProperties securityProperties,
      Clock clock) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.securityProperties = securityProperties;
    this.clock = clock;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    SecurityProperties.InitialAdmin admin = securityProperties.initialAdmin();
    User user = userRepository.findByEmailIgnoreCase(admin.email())
        .orElseGet(() -> userRepository.save(new User(
            normalize(admin.username()),
            normalize(admin.email()).toLowerCase(),
            passwordEncoder.encode(admin.password()),
            UserRole.ROLE_ADMIN,
            clock.instant())));
    if (!passwordEncoder.matches(admin.password(), user.getPassword())) {
      user.updatePassword(passwordEncoder.encode(admin.password()), Instant.now(clock));
    }
  }

  private String normalize(String value) {
    return value.trim();
  }
}

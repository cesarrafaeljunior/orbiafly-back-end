package com.orbiafly.api.support;

import com.orbiafly.api.modules.auth.application.contract.CurrentUserServiceInterface;
import com.orbiafly.api.modules.fixedexpense.application.ResourceNotFoundException;
import com.orbiafly.api.modules.user.domain.User;
import com.orbiafly.api.modules.user.infrastructure.persistence.UserRepositoryInterface;
import java.util.UUID;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestCurrentUserConfig {

  private static final UUID INITIAL_ADMIN_ID = UUID.fromString("00000000-0000-0000-0000-000000000100");

  @Bean
  @Primary
  CurrentUserServiceInterface testCurrentUserService(UserRepositoryInterface userRepository) {
    return new CurrentUserServiceInterface() {
      @Override
      public User getCurrentUser() {
        return userRepository.findById(INITIAL_ADMIN_ID)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário de teste não encontrado."));
      }
    };
  }
}

package com.orbiafly.api;

import com.orbiafly.api.infrastructure.security.CurrentUserProvider;
import java.util.UUID;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class AuthenticatedUserTestConfiguration {

  public static final UUID ADMIN_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000100");

  @Bean
  @Primary
  CurrentUserProvider testCurrentUserProvider() {
    return new CurrentUserProvider() {
      @Override
      public UUID currentUserId() {
        return ADMIN_USER_ID;
      }
    };
  }
}

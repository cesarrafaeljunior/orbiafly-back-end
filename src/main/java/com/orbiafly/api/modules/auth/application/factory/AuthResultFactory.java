package com.orbiafly.api.modules.auth.application.factory;

import com.orbiafly.api.modules.auth.application.model.AuthenticatedUserResult;
import com.orbiafly.api.modules.auth.application.model.AuthenticationResult;
import com.orbiafly.api.modules.user.domain.User;
import org.springframework.stereotype.Component;

@Component
public class AuthResultFactory {

  public AuthenticatedUserResult createAuthenticatedUserResult(User user) {
    return new AuthenticatedUserResult(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        user.getRole(),
        user.getCreatedAt(),
        user.getUpdatedAt());
  }

  public AuthenticationResult createAuthenticationResult(User user, String token, long expiresInSeconds) {
    return new AuthenticationResult(token, "Bearer", expiresInSeconds, createAuthenticatedUserResult(user));
  }
}

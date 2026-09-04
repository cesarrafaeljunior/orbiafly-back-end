package com.orbiafly.api.modules.auth.application.factory;

import com.orbiafly.api.modules.auth.application.model.AuthUserResult;
import com.orbiafly.api.modules.auth.domain.AppUser;
import org.springframework.stereotype.Component;

@Component
public class AuthUserResultFactory {

  public AuthUserResult createAuthUserResult(AppUser user) {
    return new AuthUserResult(
        user.getId(),
        user.getEmail(),
        user.getUsername(),
        user.getRoles());
  }
}

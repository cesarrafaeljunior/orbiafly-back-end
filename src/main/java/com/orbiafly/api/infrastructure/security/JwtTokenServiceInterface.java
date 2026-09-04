package com.orbiafly.api.infrastructure.security;

import com.orbiafly.api.modules.user.domain.User;

public interface JwtTokenServiceInterface {

  String createTokenForUser(User user);

  long getExpirationSeconds();
}

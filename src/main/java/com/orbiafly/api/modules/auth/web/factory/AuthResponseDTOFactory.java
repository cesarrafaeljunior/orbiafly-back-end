package com.orbiafly.api.modules.auth.web.factory;

import com.orbiafly.api.modules.auth.application.model.AuthenticatedUserResult;
import com.orbiafly.api.modules.auth.application.model.AuthenticationResult;
import com.orbiafly.api.modules.auth.web.dto.response.AuthResponseDTO;
import com.orbiafly.api.modules.auth.web.dto.response.AuthenticatedUserResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class AuthResponseDTOFactory {

  public AuthResponseDTO createAuthResponseDTO(AuthenticationResult result) {
    return new AuthResponseDTO(
        result.token(),
        result.tokenType(),
        result.expiresInSeconds(),
        createAuthenticatedUserResponseDTO(result.user()));
  }

  public AuthenticatedUserResponseDTO createAuthenticatedUserResponseDTO(AuthenticatedUserResult user) {
    return new AuthenticatedUserResponseDTO(
        user.id(), user.username(), user.email(), user.role(), user.createdAt(), user.updatedAt());
  }
}

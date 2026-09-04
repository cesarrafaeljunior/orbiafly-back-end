package com.orbiafly.api.modules.auth.web.factory;

import com.orbiafly.api.modules.auth.application.model.AuthResult;
import com.orbiafly.api.modules.auth.domain.AppRole;
import com.orbiafly.api.modules.auth.web.dto.response.AuthResponseDTO;
import com.orbiafly.api.modules.auth.web.dto.response.AuthUserResponseDTO;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class AuthResponseDTOFactory {

  public AuthResponseDTO createAuthResponseDTO(AuthResult result) {
    return new AuthResponseDTO(
        result.accessToken(),
        new AuthUserResponseDTO(
            result.user().id(),
            result.user().email(),
            result.user().username(),
            result.user().roles().stream()
                .map(AppRole::name)
                .collect(Collectors.toSet())));
  }
}

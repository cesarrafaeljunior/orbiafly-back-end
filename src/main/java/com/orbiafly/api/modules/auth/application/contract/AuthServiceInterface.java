package com.orbiafly.api.modules.auth.application.contract;

import com.orbiafly.api.modules.auth.web.dto.response.AuthResponseDTO;
import com.orbiafly.api.modules.auth.web.dto.response.AuthenticatedUserResponseDTO;

public interface AuthServiceInterface {

  AuthResponseDTO registerUser(String username, String email, String password);

  AuthResponseDTO loginUser(String login, String password);

  AuthenticatedUserResponseDTO getAuthenticatedUser();
}

package com.orbiafly.api.modules.auth.application.contract;

import com.orbiafly.api.modules.auth.web.dto.response.AuthResponseDTO;

public interface AuthServiceInterface {

  AuthResponseDTO registerUser(String email, String username, String password);

  AuthResponseDTO loginUser(String login, String password);
}

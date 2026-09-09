package com.orbiafly.api.modules.auth.application.contract;

import com.orbiafly.api.modules.auth.web.dto.response.AuthResponseDTO;

public interface AuthServiceInterface {

  AuthResponseDTO registerUser(String email, String username, String password);

  AuthResponseDTO loginUser(String login, String password);

  void changePassword(String currentPassword, String newPassword);

  void requestPasswordReset(String login);

  void confirmPasswordReset(String login, String code, String newPassword);
}

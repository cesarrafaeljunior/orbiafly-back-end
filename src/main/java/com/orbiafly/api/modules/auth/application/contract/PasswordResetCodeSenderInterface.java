package com.orbiafly.api.modules.auth.application.contract;

public interface PasswordResetCodeSenderInterface {

  void sendPasswordResetCode(String email, String username, String code);
}

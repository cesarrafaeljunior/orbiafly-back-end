package com.orbiafly.api.modules.auth.infrastructure.notification;

import com.orbiafly.api.modules.auth.application.contract.PasswordResetCodeSenderInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"local", "test"})
public class LoggingPasswordResetCodeSender implements PasswordResetCodeSenderInterface {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoggingPasswordResetCodeSender.class);

  @Override
  public void sendPasswordResetCode(String email, String username, String code) {
    LOGGER.debug("Código de recuperação local para usuário={}: {}", username, code);
  }
}

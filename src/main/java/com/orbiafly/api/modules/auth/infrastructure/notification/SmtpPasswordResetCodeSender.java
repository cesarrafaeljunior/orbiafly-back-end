package com.orbiafly.api.modules.auth.infrastructure.notification;

import com.orbiafly.api.modules.auth.application.contract.PasswordResetCodeSenderInterface;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@Profile("!local & !test")
public class SmtpPasswordResetCodeSender implements PasswordResetCodeSenderInterface {

  private final JavaMailSender mailSender;
  private final String from;

  public SmtpPasswordResetCodeSender(
      JavaMailSender mailSender,
      @Value("${MAIL_FROM:${MAIL_USERNAME}}") String from) {
    this.mailSender = mailSender;
    this.from = from;
  }

  @Override
  public void sendPasswordResetCode(String email, String username, String code) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(from);
    message.setTo(email);
    message.setSubject("Código de recuperação do OrbiaFly");
    message.setText("Olá, " + username + ". Seu código de recuperação é " + code
        + ". Ele expira em 15 minutos.");
    mailSender.send(message);
  }
}

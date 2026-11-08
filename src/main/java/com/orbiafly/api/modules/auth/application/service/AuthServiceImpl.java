package com.orbiafly.api.modules.auth.application.service;

import com.orbiafly.api.infrastructure.security.AppUserPrincipal;
import com.orbiafly.api.infrastructure.security.CurrentUserProvider;
import com.orbiafly.api.infrastructure.security.JwtService;
import com.orbiafly.api.modules.auth.application.contract.AuthServiceInterface;
import com.orbiafly.api.modules.auth.application.contract.PasswordResetCodeSenderInterface;
import com.orbiafly.api.modules.auth.application.factory.AuthUserResultFactory;
import com.orbiafly.api.modules.auth.application.model.AuthResult;
import com.orbiafly.api.modules.auth.domain.AppRole;
import com.orbiafly.api.modules.auth.domain.AppUser;
import com.orbiafly.api.modules.auth.domain.PasswordResetCode;
import com.orbiafly.api.modules.auth.infrastructure.persistence.AppUserRepositoryInterface;
import com.orbiafly.api.modules.auth.infrastructure.persistence.PasswordResetCodeRepositoryInterface;
import com.orbiafly.api.modules.auth.web.dto.response.AuthResponseDTO;
import com.orbiafly.api.modules.auth.web.factory.AuthResponseDTOFactory;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthServiceInterface {

  private static final Duration PASSWORD_RESET_CODE_TTL = Duration.ofMinutes(15);
  private static final int MAXIMUM_RESET_ATTEMPTS = 5;
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private final AppUserRepositoryInterface userRepository;
  private final PasswordResetCodeRepositoryInterface resetCodeRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final CurrentUserProvider currentUserProvider;
  private final PasswordResetCodeSenderInterface resetCodeSender;
  private final AuthUserResultFactory userResultFactory;
  private final AuthResponseDTOFactory responseDTOFactory;
  private final Clock clock;

  public AuthServiceImpl(
      AppUserRepositoryInterface userRepository,
      PasswordResetCodeRepositoryInterface resetCodeRepository,
      PasswordEncoder passwordEncoder,
      JwtService jwtService,
      CurrentUserProvider currentUserProvider,
      PasswordResetCodeSenderInterface resetCodeSender,
      AuthUserResultFactory userResultFactory,
      AuthResponseDTOFactory responseDTOFactory,
      Clock clock) {
    this.userRepository = userRepository;
    this.resetCodeRepository = resetCodeRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.currentUserProvider = currentUserProvider;
    this.resetCodeSender = resetCodeSender;
    this.userResultFactory = userResultFactory;
    this.responseDTOFactory = responseDTOFactory;
    this.clock = clock;
  }

  @Override
  @Transactional
  public AuthResponseDTO registerUser(String email, String username, String password) {
    String normalizedEmail = normalize(email).toLowerCase();
    String normalizedUsername = normalize(username);
    if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
      throw new IllegalStateException("E-mail já cadastrado.");
    }
    if (userRepository.existsByUsernameIgnoreCase(normalizedUsername)) {
      throw new IllegalStateException("Nome de usuário já utilizado.");
    }
    AppUser user = new AppUser(
        normalizedEmail,
        normalizedUsername,
        passwordEncoder.encode(password),
        Set.of(AppRole.ROLE_USER),
        clock.instant());
    return authResponse(userRepository.save(user));
  }

  @Override
  @Transactional(readOnly = true)
  public AuthResponseDTO loginUser(String login, String password) {
    AppUser user = findUserByLogin(login);
    if (!user.isActive() || !passwordEncoder.matches(password, user.getPasswordHash())) {
      throw new BadCredentialsException("Credenciais inválidas.");
    }
    return authResponse(user);
  }

  @Override
  @Transactional
  public void changePassword(String currentPassword, String newPassword) {
    AppUser user = userRepository.findById(currentUserProvider.currentUserId())
        .orElseThrow(() -> new BadCredentialsException("Usuário autenticado não encontrado."));
    if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
      throw new IllegalArgumentException("Senha atual inválida.");
    }
    user.updatePasswordHash(passwordEncoder.encode(newPassword), clock.instant());
  }

  @Override
  @Transactional
  public void requestPasswordReset(String login) {
    userRepository.findByEmailIgnoreCase(normalize(login))
        .or(() -> userRepository.findByUsernameIgnoreCase(normalize(login)))
        .filter(AppUser::isActive)
        .ifPresent(this::createAndSendResetCode);
  }

  @Override
  @Transactional(noRollbackFor = BadCredentialsException.class)
  public void confirmPasswordReset(String login, String code, String newPassword) {
    AppUser user = findUserByLogin(login);
    if (!user.isActive()) {
      throw new BadCredentialsException("Código inválido ou expirado.");
    }

    Instant now = clock.instant();
    List<PasswordResetCode> activeCodes = resetCodeRepository
        .findByUserIdAndUsedAtIsNullOrderByCreatedAtDesc(user.getId());
    PasswordResetCode validCode = activeCodes.stream()
        .filter(resetCode -> resetCode.getExpiresAt().isAfter(now))
        .findFirst()
        .orElseThrow(() -> new BadCredentialsException("Código inválido ou expirado."));
    if (!passwordEncoder.matches(code, validCode.getCodeHash())) {
      validCode.registerFailedAttempt(now, MAXIMUM_RESET_ATTEMPTS);
      throw new BadCredentialsException("Código inválido ou expirado.");
    }

    user.updatePasswordHash(passwordEncoder.encode(newPassword), now);
    activeCodes.forEach(resetCode -> resetCode.markUsed(now));
    validCode.markUsed(now);
  }

  private void createAndSendResetCode(AppUser user) {
    Instant now = clock.instant();
    resetCodeRepository.findByUserIdAndUsedAtIsNullOrderByCreatedAtDesc(user.getId())
        .forEach(resetCode -> resetCode.markUsed(now));

    String code = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
    resetCodeRepository.save(new PasswordResetCode(
        user,
        passwordEncoder.encode(code),
        now,
        now.plus(PASSWORD_RESET_CODE_TTL)));
    resetCodeSender.sendPasswordResetCode(user.getEmail(), user.getUsername(), code);
  }

  private AppUser findUserByLogin(String login) {
    return userRepository.findByEmailIgnoreCase(normalize(login))
        .or(() -> userRepository.findByUsernameIgnoreCase(normalize(login)))
        .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas."));
  }

  private AuthResponseDTO authResponse(AppUser user) {
    AppUserPrincipal principal = new AppUserPrincipal(user);
    return responseDTOFactory.createAuthResponseDTO(
        new AuthResult(
            jwtService.createToken(principal),
            userResultFactory.createAuthUserResult(user)));
  }

  private String normalize(String value) {
    return value.trim();
  }
}

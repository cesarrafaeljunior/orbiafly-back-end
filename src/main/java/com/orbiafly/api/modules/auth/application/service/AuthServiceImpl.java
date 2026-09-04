package com.orbiafly.api.modules.auth.application.service;

import com.orbiafly.api.infrastructure.security.JwtTokenServiceInterface;
import com.orbiafly.api.modules.auth.application.contract.AuthServiceInterface;
import com.orbiafly.api.modules.auth.application.contract.CurrentUserServiceInterface;
import com.orbiafly.api.modules.auth.application.factory.AuthResultFactory;
import com.orbiafly.api.modules.auth.application.model.AuthenticationResult;
import com.orbiafly.api.modules.auth.web.dto.response.AuthResponseDTO;
import com.orbiafly.api.modules.auth.web.dto.response.AuthenticatedUserResponseDTO;
import com.orbiafly.api.modules.auth.web.factory.AuthResponseDTOFactory;
import com.orbiafly.api.modules.user.domain.User;
import com.orbiafly.api.modules.user.domain.UserRole;
import com.orbiafly.api.modules.user.infrastructure.persistence.UserRepositoryInterface;
import java.time.Clock;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthServiceInterface {

  private final UserRepositoryInterface userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenServiceInterface jwtTokenService;
  private final CurrentUserServiceInterface currentUserService;
  private final AuthResultFactory authResultFactory;
  private final AuthResponseDTOFactory authResponseDTOFactory;
  private final Clock clock;

  public AuthServiceImpl(
      UserRepositoryInterface userRepository,
      PasswordEncoder passwordEncoder,
      JwtTokenServiceInterface jwtTokenService,
      CurrentUserServiceInterface currentUserService,
      AuthResultFactory authResultFactory,
      AuthResponseDTOFactory authResponseDTOFactory,
      Clock clock) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtTokenService = jwtTokenService;
    this.currentUserService = currentUserService;
    this.authResultFactory = authResultFactory;
    this.authResponseDTOFactory = authResponseDTOFactory;
    this.clock = clock;
  }

  @Override
  @Transactional
  public AuthResponseDTO registerUser(String username, String email, String password) {
    String normalizedUsername = username.trim();
    String normalizedEmail = email.trim().toLowerCase();
    if (userRepository.existsByUsernameIgnoreCase(normalizedUsername)) {
      throw new IllegalStateException("Nome de usuário já cadastrado.");
    }
    if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
      throw new IllegalStateException("E-mail já cadastrado.");
    }
    User user = userRepository.save(new User(
        normalizedUsername,
        normalizedEmail,
        passwordEncoder.encode(password),
        UserRole.ROLE_USER,
        clock.instant()));
    return createAuthResponseDTO(user);
  }

  @Override
  @Transactional(readOnly = true)
  public AuthResponseDTO loginUser(String login, String password) {
    String normalizedLogin = login.trim();
    User user = userRepository.findByEmailIgnoreCase(normalizedLogin)
        .or(() -> userRepository.findByUsernameIgnoreCase(normalizedLogin))
        .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas."));
    if (!passwordEncoder.matches(password, user.getPassword())) {
      throw new BadCredentialsException("Credenciais inválidas.");
    }
    return createAuthResponseDTO(user);
  }

  @Override
  @Transactional(readOnly = true)
  public AuthenticatedUserResponseDTO getAuthenticatedUser() {
    return authResponseDTOFactory.createAuthenticatedUserResponseDTO(
        authResultFactory.createAuthenticatedUserResult(currentUserService.getCurrentUser()));
  }

  private AuthResponseDTO createAuthResponseDTO(User user) {
    AuthenticationResult result = authResultFactory.createAuthenticationResult(
        user,
        jwtTokenService.createTokenForUser(user),
        jwtTokenService.getExpirationSeconds());
    return authResponseDTOFactory.createAuthResponseDTO(result);
  }
}

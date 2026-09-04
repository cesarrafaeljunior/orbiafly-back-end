package com.orbiafly.api.modules.auth.application.service;

import com.orbiafly.api.infrastructure.security.AppUserPrincipal;
import com.orbiafly.api.infrastructure.security.JwtService;
import com.orbiafly.api.modules.auth.application.contract.AuthServiceInterface;
import com.orbiafly.api.modules.auth.application.factory.AuthUserResultFactory;
import com.orbiafly.api.modules.auth.application.model.AuthResult;
import com.orbiafly.api.modules.auth.domain.AppRole;
import com.orbiafly.api.modules.auth.domain.AppUser;
import com.orbiafly.api.modules.auth.infrastructure.persistence.AppUserRepositoryInterface;
import com.orbiafly.api.modules.auth.web.dto.response.AuthResponseDTO;
import com.orbiafly.api.modules.auth.web.factory.AuthResponseDTOFactory;
import java.time.Clock;
import java.util.Set;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthServiceInterface {

  private final AppUserRepositoryInterface userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthUserResultFactory userResultFactory;
  private final AuthResponseDTOFactory responseDTOFactory;
  private final Clock clock;

  public AuthServiceImpl(
      AppUserRepositoryInterface userRepository,
      PasswordEncoder passwordEncoder,
      JwtService jwtService,
      AuthUserResultFactory userResultFactory,
      AuthResponseDTOFactory responseDTOFactory,
      Clock clock) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
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
    AppUser user = userRepository.findByEmailIgnoreCase(login)
        .or(() -> userRepository.findByUsernameIgnoreCase(login))
        .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas."));
    if (!user.isActive() || !passwordEncoder.matches(password, user.getPasswordHash())) {
      throw new BadCredentialsException("Credenciais inválidas.");
    }
    return authResponse(user);
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

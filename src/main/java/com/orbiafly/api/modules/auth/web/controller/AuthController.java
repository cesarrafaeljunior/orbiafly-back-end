package com.orbiafly.api.modules.auth.web.controller;

import com.orbiafly.api.infrastructure.security.AuthCookieService;
import com.orbiafly.api.modules.auth.application.contract.AuthServiceInterface;
import com.orbiafly.api.modules.auth.web.dto.request.ChangePasswordRequestDTO;
import com.orbiafly.api.modules.auth.web.dto.request.ConfirmPasswordResetRequestDTO;
import com.orbiafly.api.modules.auth.web.dto.request.LoginRequestDTO;
import com.orbiafly.api.modules.auth.web.dto.request.RegisterRequestDTO;
import com.orbiafly.api.modules.auth.web.dto.request.RequestPasswordResetRequestDTO;
import com.orbiafly.api.modules.auth.web.dto.response.AuthResponseDTO;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthServiceInterface service;
  private final AuthCookieService cookieService;

  public AuthController(AuthServiceInterface service, AuthCookieService cookieService) {
    this.service = service;
    this.cookieService = cookieService;
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public AuthResponseDTO registerUser(
      @Valid @RequestBody RegisterRequestDTO request,
      HttpServletResponse response) {
    return authenticatedResponse(
        service.registerUser(request.email(), request.username(), request.password()), response);
  }

  @PostMapping("/login")
  public AuthResponseDTO loginUser(
      @Valid @RequestBody LoginRequestDTO request,
      HttpServletResponse response) {
    return authenticatedResponse(service.loginUser(request.login(), request.password()), response);
  }

  @PostMapping("/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void logout(HttpServletResponse response) {
    cookieService.clear(response);
  }

  @PostMapping("/change-password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void changePassword(@Valid @RequestBody ChangePasswordRequestDTO request) {
    service.changePassword(request.currentPassword(), request.newPassword());
  }

  @PostMapping("/password-reset/request")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void requestPasswordReset(@Valid @RequestBody RequestPasswordResetRequestDTO request) {
    service.requestPasswordReset(request.login());
  }

  @PostMapping("/password-reset/confirm")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void confirmPasswordReset(@Valid @RequestBody ConfirmPasswordResetRequestDTO request) {
    service.confirmPasswordReset(request.login(), request.code(), request.newPassword());
  }

  private AuthResponseDTO authenticatedResponse(
      AuthResponseDTO authResponse,
      HttpServletResponse response) {
    cookieService.write(response, authResponse.accessToken());
    return authResponse;
  }
}

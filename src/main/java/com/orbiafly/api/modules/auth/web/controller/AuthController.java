package com.orbiafly.api.modules.auth.web.controller;

import com.orbiafly.api.modules.auth.application.contract.AuthServiceInterface;
import com.orbiafly.api.modules.auth.web.dto.request.LoginRequestDTO;
import com.orbiafly.api.modules.auth.web.dto.request.RegisterUserRequestDTO;
import com.orbiafly.api.modules.auth.web.dto.response.AuthResponseDTO;
import com.orbiafly.api.modules.auth.web.dto.response.AuthenticatedUserResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthServiceInterface authService;

  public AuthController(AuthServiceInterface authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public AuthResponseDTO registerUser(@Valid @RequestBody RegisterUserRequestDTO request) {
    return authService.registerUser(request.username(), request.email(), request.password());
  }

  @PostMapping("/login")
  public AuthResponseDTO loginUser(@Valid @RequestBody LoginRequestDTO request) {
    return authService.loginUser(request.login(), request.password());
  }

  @PostMapping("/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void logoutUser() {
  }

  @GetMapping("/me")
  public AuthenticatedUserResponseDTO getAuthenticatedUser() {
    return authService.getAuthenticatedUser();
  }
}

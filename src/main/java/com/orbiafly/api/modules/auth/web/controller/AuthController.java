package com.orbiafly.api.modules.auth.web.controller;

import com.orbiafly.api.modules.auth.application.contract.AuthServiceInterface;
import com.orbiafly.api.modules.auth.web.dto.request.LoginRequestDTO;
import com.orbiafly.api.modules.auth.web.dto.request.RegisterRequestDTO;
import com.orbiafly.api.modules.auth.web.dto.response.AuthResponseDTO;
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

  public AuthController(AuthServiceInterface service) {
    this.service = service;
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public AuthResponseDTO registerUser(@Valid @RequestBody RegisterRequestDTO request) {
    return service.registerUser(request.email(), request.username(), request.password());
  }

  @PostMapping("/login")
  public AuthResponseDTO loginUser(@Valid @RequestBody LoginRequestDTO request) {
    return service.loginUser(request.login(), request.password());
  }
}

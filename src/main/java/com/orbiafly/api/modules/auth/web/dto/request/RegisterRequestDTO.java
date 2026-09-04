package com.orbiafly.api.modules.auth.web.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
    @NotBlank @Email @Size(max = 180) String email,
    @NotBlank @Size(min = 3, max = 80) String username,
    @NotBlank @Size(min = 8, max = 120) String password) {
}

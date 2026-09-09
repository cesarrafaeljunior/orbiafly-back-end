package com.orbiafly.api.modules.auth.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ConfirmPasswordResetRequestDTO(
    @NotBlank String login,
    @NotBlank @Pattern(regexp = "\\d{6}") String code,
    @NotBlank @Size(min = 8, max = 120) String newPassword) {
}

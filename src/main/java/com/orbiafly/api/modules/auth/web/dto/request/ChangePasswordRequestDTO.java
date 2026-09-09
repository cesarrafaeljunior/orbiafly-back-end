package com.orbiafly.api.modules.auth.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequestDTO(
    @NotBlank String currentPassword,
    @NotBlank @Size(min = 8, max = 120) String newPassword) {
}

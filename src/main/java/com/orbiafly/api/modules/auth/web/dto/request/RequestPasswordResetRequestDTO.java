package com.orbiafly.api.modules.auth.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RequestPasswordResetRequestDTO(@NotBlank String login) {
}

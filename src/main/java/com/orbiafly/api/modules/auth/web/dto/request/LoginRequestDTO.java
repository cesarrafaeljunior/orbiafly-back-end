package com.orbiafly.api.modules.auth.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
    @NotBlank(message = "Informe e-mail ou usuário.")
    String login,

    @NotBlank(message = "Informe a senha.")
    String password) {
}

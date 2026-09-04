package com.orbiafly.api.modules.auth.web.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserRequestDTO(
    @NotBlank(message = "Informe o nome de usuário.")
    @Size(max = 80, message = "O nome de usuário deve ter no máximo 80 caracteres.")
    String username,

    @NotBlank(message = "Informe o e-mail.")
    @Email(message = "Informe um e-mail válido.")
    @Size(max = 160, message = "O e-mail deve ter no máximo 160 caracteres.")
    String email,

    @NotBlank(message = "Informe a senha.")
    @Size(min = 4, max = 80, message = "A senha deve ter entre 4 e 80 caracteres.")
    String password) {
}

package com.orbiafly.api.modules.financialaccount.web.dto.request;

import com.orbiafly.api.modules.financialaccount.domain.FinancialAccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateFinancialAccountRequestDTO(
    @NotBlank(message = "Informe o nome da conta.")
    @Size(max = 120, message = "O nome deve possuir no máximo 120 caracteres.")
    String name,

    @NotNull(message = "Selecione o tipo da conta.")
    FinancialAccountType type,

    @Size(max = 120, message = "A instituição deve possuir no máximo 120 caracteres.")
    String institution) {
}

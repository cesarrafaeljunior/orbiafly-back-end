package com.orbiafly.api.modules.financialaccount.web.dto.request;

import com.orbiafly.api.modules.financialaccount.domain.FinancialAccountType;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateFinancialAccountRequestDTO(
    @NotBlank(message = "Informe o nome da conta.")
    @Size(max = 120, message = "O nome deve possuir no máximo 120 caracteres.")
    String name,

    @NotNull(message = "Selecione o tipo da conta.")
    FinancialAccountType type,

    @Size(max = 120, message = "A instituição deve possuir no máximo 120 caracteres.")
    String institution,

    @NotNull(message = "Informe o saldo inicial.")
    @Digits(integer = 13, fraction = 2, message = "Informe um saldo válido com até duas casas decimais.")
    BigDecimal openingBalance) {
}

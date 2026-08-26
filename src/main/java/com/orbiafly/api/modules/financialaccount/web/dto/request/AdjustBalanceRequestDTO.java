package com.orbiafly.api.modules.financialaccount.web.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record AdjustBalanceRequestDTO(
    @NotNull(message = "Informe o saldo real da conta.")
    @Digits(integer = 13, fraction = 2, message = "Informe um saldo válido com até duas casas decimais.")
    BigDecimal targetBalance,

    @Size(max = 180, message = "A descrição deve possuir no máximo 180 caracteres.")
    String description) {
}

package com.orbiafly.api.modules.creditcard.web.dto.request;

import com.orbiafly.api.modules.creditcard.domain.CreditCardBrand;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateCreditCardRequestDTO(
    @NotBlank(message = "Informe o nome do cartão.")
    @Size(max = 120, message = "O nome deve possuir no máximo 120 caracteres.")
    String name,

    @NotNull(message = "Selecione a bandeira do cartão.")
    CreditCardBrand brand,

    @Pattern(regexp = "^$|^[0-9]{4}$", message = "Os últimos 4 dígitos devem conter exatamente 4 números.")
    String lastFourDigits,

    @NotNull(message = "Informe o limite total.")
    @Digits(integer = 13, fraction = 2, message = "Informe um limite válido com até duas casas decimais.")
    BigDecimal totalLimit,

    @NotNull(message = "Informe o dia de fechamento.")
    @Min(value = 1, message = "O dia de fechamento deve estar entre 1 e 31.")
    @Max(value = 31, message = "O dia de fechamento deve estar entre 1 e 31.")
    Integer closingDay,

    @NotNull(message = "Informe o dia de vencimento.")
    @Min(value = 1, message = "O dia de vencimento deve estar entre 1 e 31.")
    @Max(value = 31, message = "O dia de vencimento deve estar entre 1 e 31.")
    Integer dueDay) {
}

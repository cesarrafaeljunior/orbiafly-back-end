package com.orbiafly.api.modules.installmentexpense.web.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateInstallmentPurchaseRequestDTO(
    @NotBlank(message = "Informe a descrição.")
    @Size(max = 180, message = "A descrição deve ter no máximo 180 caracteres.")
    String description,

    @NotNull(message = "Informe o valor total.")
    @DecimalMin(value = "0.01", message = "O valor total deve ser maior que zero.")
    @Digits(integer = 13, fraction = 2, message = "Informe um valor monetário válido.")
    BigDecimal totalAmount,

    @Min(value = 1, message = "Informe pelo menos uma parcela.")
    @Max(value = 120, message = "O limite é de 120 parcelas.")
    int installmentCount,

    @NotNull(message = "Informe a data da primeira parcela.")
    LocalDate firstInstallmentDate) {
}

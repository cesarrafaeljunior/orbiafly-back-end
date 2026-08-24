package com.orbiafly.api.modules.fixedexpense.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public record UpdateFixedExpenseRequest(
    @NotBlank(message = "Informe o nome.")
    @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.")
    String name,

    @NotNull(message = "Informe o valor padrão.")
    @DecimalMin(value = "0.00", message = "O valor não pode ser negativo.")
    @Digits(integer = 13, fraction = 2, message = "O valor deve ter no máximo duas casas decimais.")
    BigDecimal defaultAmount,

    @NotNull(message = "Informe o dia do vencimento.")
    @Min(value = 1, message = "O vencimento deve estar entre os dias 1 e 31.")
    @Max(value = 31, message = "O vencimento deve estar entre os dias 1 e 31.")
    Integer dueDay,

    @NotNull(message = "Selecione uma categoria.")
    UUID categoryId,

    @NotNull(message = "Informe se a despesa está ativa.")
    Boolean active) {
}

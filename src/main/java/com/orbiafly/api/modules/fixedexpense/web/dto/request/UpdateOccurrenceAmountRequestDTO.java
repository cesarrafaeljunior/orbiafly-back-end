package com.orbiafly.api.modules.fixedexpense.web.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record UpdateOccurrenceAmountRequestDTO(
    @NotNull(message = "Informe o valor mensal.")
    @DecimalMin(value = "0.00", message = "O valor não pode ser negativo.")
    @Digits(integer = 13, fraction = 2, message = "O valor deve ter no máximo duas casas decimais.")
    BigDecimal amount) {
}

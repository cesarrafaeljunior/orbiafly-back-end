package com.orbiafly.api.modules.oneoffexpense.web.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateOneOffExpenseRequestDTO(
    @NotBlank @Size(max = 120) String name,
    @NotNull @DecimalMin(value = "0.01") @Digits(integer = 13, fraction = 2) BigDecimal amount,
    @NotNull LocalDate expenseDate,
    @NotNull UUID categoryId) {
}

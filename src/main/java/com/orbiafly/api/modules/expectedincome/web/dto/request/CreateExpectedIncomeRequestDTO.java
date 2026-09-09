package com.orbiafly.api.modules.expectedincome.web.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateExpectedIncomeRequestDTO(
    @NotBlank(message = "Informe a descrição.")
    @Size(max = 160, message = "A descrição deve ter no máximo 160 caracteres.")
    String description,

    @NotNull(message = "Informe o valor.")
    @DecimalMin(value = "0.01", message = "Informe um valor maior que zero.")
    BigDecimal amount,

    @NotNull(message = "Informe a data prevista.")
    LocalDate expectedDate,

    UUID financialAccountId,

    @Size(max = 500, message = "A observação deve ter no máximo 500 caracteres.")
    String notes) {
}

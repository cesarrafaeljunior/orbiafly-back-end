package com.orbiafly.api.modules.creditcard.web.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record UpdateCreditCardInstallmentAmountRequestDTO(
    @NotNull @DecimalMin(value = "0.01") BigDecimal amount) {
}

package com.orbiafly.api.modules.creditcard.web.dto.request;

import com.orbiafly.api.modules.creditcard.domain.CreditCardInstallmentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateCreditCardInstallmentStatusRequestDTO(
    @NotNull CreditCardInstallmentStatus status) {
}

package com.orbiafly.api.modules.fixedexpense.web;

import com.orbiafly.api.modules.fixedexpense.domain.PaymentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOccurrenceStatusRequest(
    @NotNull(message = "Informe o status.") PaymentStatus status) {
}

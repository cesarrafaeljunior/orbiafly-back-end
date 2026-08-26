package com.orbiafly.api.modules.fixedexpense.web.dto.request;

import com.orbiafly.api.modules.fixedexpense.domain.PaymentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOccurrenceStatusRequestDTO(
    @NotNull(message = "Informe o status.") PaymentStatus status) {
}

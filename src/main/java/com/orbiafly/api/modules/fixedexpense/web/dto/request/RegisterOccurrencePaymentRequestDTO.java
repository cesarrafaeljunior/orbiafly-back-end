package com.orbiafly.api.modules.fixedexpense.web.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record RegisterOccurrencePaymentRequestDTO(
    @NotNull(message = "Selecione a conta utilizada no pagamento.")
    UUID financialAccountId,

    @NotNull(message = "Informe a data do pagamento.")
    LocalDate paymentDate) {
}

package com.orbiafly.api.modules.fixedexpense.web;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record RegisterOccurrencePaymentRequest(
    @NotNull(message = "Selecione a conta utilizada no pagamento.")
    UUID financialAccountId,

    @NotNull(message = "Informe a data do pagamento.")
    LocalDate paymentDate) {
}

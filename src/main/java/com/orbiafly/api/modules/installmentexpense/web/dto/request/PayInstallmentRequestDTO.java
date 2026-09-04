package com.orbiafly.api.modules.installmentexpense.web.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record PayInstallmentRequestDTO(
    @NotNull(message = "Informe a conta financeira.")
    UUID financialAccountId,

    @NotNull(message = "Informe a data de pagamento.")
    LocalDate paymentDate) {
}

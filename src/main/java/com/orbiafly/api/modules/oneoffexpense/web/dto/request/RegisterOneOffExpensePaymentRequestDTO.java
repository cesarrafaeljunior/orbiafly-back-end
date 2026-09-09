package com.orbiafly.api.modules.oneoffexpense.web.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record RegisterOneOffExpensePaymentRequestDTO(
    @NotNull UUID financialAccountId,
    @NotNull LocalDate paymentDate) {
}

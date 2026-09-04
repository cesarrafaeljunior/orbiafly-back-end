package com.orbiafly.api.modules.loan.web.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RegisterLoanEntryRequestDTO(
    @NotNull UUID financialAccountId,
    @DecimalMin("0.01") BigDecimal amount,
    @NotNull LocalDate occurredOn) {
}

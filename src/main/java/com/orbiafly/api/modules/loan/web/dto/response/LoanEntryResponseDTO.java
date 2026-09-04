package com.orbiafly.api.modules.loan.web.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record LoanEntryResponseDTO(
    UUID id,
    UUID installmentId,
    Integer installmentNumber,
    BigDecimal amount,
    LocalDate occurredOn,
    UUID financialAccountId,
    String financialAccountName,
    UUID financialTransactionId,
    Instant createdAt) {
}

package com.orbiafly.api.modules.loan.application.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record LoanEntryResult(
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

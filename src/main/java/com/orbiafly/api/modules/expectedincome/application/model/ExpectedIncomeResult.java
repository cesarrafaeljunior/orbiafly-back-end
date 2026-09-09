package com.orbiafly.api.modules.expectedincome.application.model;

import com.orbiafly.api.modules.expectedincome.domain.ExpectedIncomeStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ExpectedIncomeResult(
    UUID id,
    String description,
    BigDecimal amount,
    LocalDate expectedDate,
    UUID financialAccountId,
    String financialAccountName,
    ExpectedIncomeStatus status,
    Instant receivedAt,
    UUID financialTransactionId,
    String notes,
    Instant createdAt,
    Instant updatedAt) {
}

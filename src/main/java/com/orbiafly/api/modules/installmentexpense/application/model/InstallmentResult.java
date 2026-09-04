package com.orbiafly.api.modules.installmentexpense.application.model;

import com.orbiafly.api.modules.fixedexpense.domain.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record InstallmentResult(
    UUID id,
    int installmentNumber,
    BigDecimal amount,
    LocalDate dueDate,
    PaymentStatus status,
    Instant paidAt,
    UUID financialAccountId,
    UUID paymentTransactionId,
    Instant createdAt,
    Instant updatedAt) {
}

package com.orbiafly.api.modules.installmentexpense.application.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record InstallmentPurchaseResult(
    UUID id,
    String description,
    BigDecimal totalAmount,
    int installmentCount,
    LocalDate firstInstallmentDate,
    BigDecimal paidAmount,
    BigDecimal pendingAmount,
    List<InstallmentResult> installments,
    Instant createdAt,
    Instant updatedAt) {
}

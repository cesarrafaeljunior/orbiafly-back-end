package com.orbiafly.api.modules.creditcard.application.model;

import com.orbiafly.api.modules.creditcard.domain.CreditCardInstallmentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CreditCardInstallmentResult(
    UUID id,
    UUID purchaseId,
    int installmentNumber,
    BigDecimal amount,
    int competenceYear,
    int competenceMonth,
    LocalDate dueDate,
    CreditCardInstallmentStatus status,
    Instant createdAt) {
}

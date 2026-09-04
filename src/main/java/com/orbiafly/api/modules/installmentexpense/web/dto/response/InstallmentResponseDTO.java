package com.orbiafly.api.modules.installmentexpense.web.dto.response;

import com.orbiafly.api.modules.fixedexpense.domain.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record InstallmentResponseDTO(
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

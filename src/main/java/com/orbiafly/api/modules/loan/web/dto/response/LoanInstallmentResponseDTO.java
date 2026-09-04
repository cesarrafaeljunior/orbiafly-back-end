package com.orbiafly.api.modules.loan.web.dto.response;

import com.orbiafly.api.modules.loan.domain.LoanInstallmentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record LoanInstallmentResponseDTO(
    UUID id,
    int installmentNumber,
    BigDecimal amount,
    LocalDate dueDate,
    LoanInstallmentStatus status,
    LocalDate completedAt,
    Instant createdAt,
    Instant updatedAt) {
}

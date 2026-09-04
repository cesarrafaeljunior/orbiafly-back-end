package com.orbiafly.api.modules.loan.web.dto.response;

import com.orbiafly.api.modules.loan.domain.LoanDirection;
import com.orbiafly.api.modules.loan.domain.LoanStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record LoanResponseDTO(
    UUID id,
    String name,
    LoanDirection direction,
    String counterparty,
    BigDecimal totalAmount,
    LocalDate startDate,
    Integer installmentCount,
    BigDecimal installmentAmount,
    LocalDate firstDueDate,
    String interestRate,
    String notes,
    LoanStatus status,
    BigDecimal settledAmount,
    BigDecimal remainingAmount,
    BigDecimal progressPercentage,
    int completedInstallmentCount,
    LoanInstallmentResponseDTO nextInstallment,
    LocalDate estimatedEndDate,
    List<LoanInstallmentResponseDTO> installments,
    List<LoanEntryResponseDTO> entries,
    Instant deletedAt,
    Instant createdAt,
    Instant updatedAt) {
}

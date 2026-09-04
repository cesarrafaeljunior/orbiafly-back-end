package com.orbiafly.api.modules.installmentexpense.web.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record InstallmentPurchaseResponseDTO(
    UUID id,
    String description,
    BigDecimal totalAmount,
    int installmentCount,
    LocalDate firstInstallmentDate,
    BigDecimal paidAmount,
    BigDecimal pendingAmount,
    List<InstallmentResponseDTO> installments,
    Instant createdAt,
    Instant updatedAt) {
}

package com.orbiafly.api.modules.oneoffexpense.web.dto.response;

import com.orbiafly.api.modules.fixedexpense.domain.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record OneOffExpenseResponseDTO(
    UUID id,
    String name,
    BigDecimal amount,
    LocalDate expenseDate,
    UUID categoryId,
    String category,
    PaymentStatus status,
    Instant paidAt,
    UUID financialAccountId,
    String financialAccountName,
    boolean overdue,
    Integer daysUntilDue,
    Integer daysOverdue,
    Instant createdAt,
    Instant updatedAt) {
}

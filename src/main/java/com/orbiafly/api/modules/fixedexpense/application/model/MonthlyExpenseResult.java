package com.orbiafly.api.modules.fixedexpense.application.model;

import com.orbiafly.api.modules.fixedexpense.domain.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record MonthlyExpenseResult(
    UUID occurrenceId,
    UUID fixedExpenseId,
    String name,
    BigDecimal amount,
    BigDecimal defaultAmount,
    LocalDate dueDate,
    int dueDay,
    UUID categoryId,
    String category,
    PaymentStatus status,
    Instant paidAt,
    UUID financialAccountId,
    String financialAccountName,
    boolean overdue,
    Integer daysUntilDue,
    Integer daysOverdue,
    boolean active) {
}

package com.orbiafly.api.modules.fixedexpense.application.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record FixedExpenseResult(
    UUID id,
    String name,
    BigDecimal defaultAmount,
    int dueDay,
    UUID categoryId,
    String category,
    boolean active,
    Instant createdAt,
    Instant updatedAt) {
}

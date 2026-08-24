package com.orbiafly.api.modules.fixedexpense.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record FixedExpenseView(
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

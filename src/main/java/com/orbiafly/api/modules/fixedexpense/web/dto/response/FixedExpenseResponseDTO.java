package com.orbiafly.api.modules.fixedexpense.web.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record FixedExpenseResponseDTO(
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

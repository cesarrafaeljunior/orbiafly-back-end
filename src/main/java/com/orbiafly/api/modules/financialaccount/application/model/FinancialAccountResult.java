package com.orbiafly.api.modules.financialaccount.application.model;

import com.orbiafly.api.modules.financialaccount.domain.FinancialAccountType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record FinancialAccountResult(
    UUID id,
    String name,
    FinancialAccountType type,
    String institution,
    BigDecimal openingBalance,
    BigDecimal currentBalance,
    boolean active,
    Instant createdAt,
    Instant updatedAt) {
}

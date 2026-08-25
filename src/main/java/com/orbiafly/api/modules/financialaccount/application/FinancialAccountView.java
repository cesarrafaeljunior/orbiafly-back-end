package com.orbiafly.api.modules.financialaccount.application;

import com.orbiafly.api.modules.financialaccount.domain.FinancialAccountType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record FinancialAccountView(
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

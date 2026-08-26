package com.orbiafly.api.modules.financialaccount.web.dto.response;

import com.orbiafly.api.modules.financialaccount.domain.FinancialAccountType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record FinancialAccountResponseDTO(
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

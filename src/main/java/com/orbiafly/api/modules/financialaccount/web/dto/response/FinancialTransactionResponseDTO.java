package com.orbiafly.api.modules.financialaccount.web.dto.response;

import com.orbiafly.api.modules.financialaccount.domain.FinancialReferenceType;
import com.orbiafly.api.modules.financialaccount.domain.FinancialTransactionType;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record FinancialTransactionResponseDTO(
    UUID id,
    UUID financialAccountId,
    FinancialTransactionType type,
    BigDecimal amount,
    BigDecimal balanceEffect,
    String description,
    LocalDate occurredOn,
    FinancialReferenceType referenceType,
    UUID referenceId,
    UUID reversalOfId,
    Instant createdAt) {
}

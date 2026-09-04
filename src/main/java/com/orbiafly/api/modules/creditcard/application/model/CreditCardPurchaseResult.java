package com.orbiafly.api.modules.creditcard.application.model;

import com.orbiafly.api.modules.creditcard.domain.CreditCardBrand;
import com.orbiafly.api.modules.creditcard.domain.CreditCardPurchaseStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreditCardPurchaseResult(
    UUID id,
    String description,
    BigDecimal totalAmount,
    LocalDate purchaseDate,
    int installmentCount,
    BigDecimal installmentAmount,
    UUID creditCardId,
    String creditCardName,
    CreditCardBrand creditCardBrand,
    String creditCardLastFourDigits,
    CreditCardPurchaseStatus status,
    int completedInstallmentCount,
    int futureInstallmentCount,
    CreditCardInstallmentResult nextInstallment,
    List<CreditCardInstallmentResult> installments,
    Instant deletedAt,
    Instant createdAt,
    Instant updatedAt) {
}

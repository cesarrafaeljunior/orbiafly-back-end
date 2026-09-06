package com.orbiafly.api.modules.creditcard.web.dto.response;

import com.orbiafly.api.modules.creditcard.domain.CreditCardBrand;
import com.orbiafly.api.modules.creditcard.domain.CreditCardInstallmentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CreditCardMonthlyPurchaseInstallmentResponseDTO(
    UUID installmentId,
    UUID purchaseId,
    String description,
    BigDecimal amount,
    BigDecimal totalAmount,
    LocalDate purchaseDate,
    int installmentNumber,
    int installmentCount,
    UUID creditCardId,
    String creditCardName,
    CreditCardBrand creditCardBrand,
    String creditCardLastFourDigits,
    int competenceYear,
    int competenceMonth,
    LocalDate dueDate,
    CreditCardInstallmentStatus status,
    Instant paidAt,
    int paidInstallmentCount,
    int pendingInstallmentCount,
    int remainingInstallmentCount,
    int endingYear,
    int endingMonth,
    long monthsUntilEnd,
    Instant createdAt,
    Instant updatedAt) {
}

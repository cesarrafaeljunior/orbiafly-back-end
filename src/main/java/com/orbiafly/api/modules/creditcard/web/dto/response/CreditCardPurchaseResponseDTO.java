package com.orbiafly.api.modules.creditcard.web.dto.response;

import com.orbiafly.api.modules.creditcard.domain.CreditCardBrand;
import com.orbiafly.api.modules.creditcard.domain.CreditCardPurchaseStatus;
import com.orbiafly.api.modules.creditcard.domain.CreditCardPurchaseType;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreditCardPurchaseResponseDTO(
    UUID id,
    String description,
    BigDecimal totalAmount,
    LocalDate purchaseDate,
    CreditCardPurchaseType purchaseType,
    int installmentCount,
    BigDecimal installmentAmount,
    int invoiceCompetenceYear,
    int invoiceCompetenceMonth,
    LocalDate invoiceDueDate,
    UUID creditCardId,
    String creditCardName,
    CreditCardBrand creditCardBrand,
    String creditCardLastFourDigits,
    CreditCardPurchaseStatus status,
    int completedInstallmentCount,
    int futureInstallmentCount,
    CreditCardInstallmentResponseDTO nextInstallment,
    List<CreditCardInstallmentResponseDTO> installments,
    Instant deletedAt,
    Instant createdAt,
    Instant updatedAt) {
}

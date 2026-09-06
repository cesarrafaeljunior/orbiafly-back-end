package com.orbiafly.api.modules.creditcard.web.dto.response;

import java.math.BigDecimal;

public record CreditCardMonthlyPurchaseSummaryResponseDTO(
    BigDecimal total,
    BigDecimal paidTotal,
    BigDecimal pendingTotal,
    int totalCount,
    int paidCount,
    int pendingCount,
    BigDecimal paidPercentage) {
}

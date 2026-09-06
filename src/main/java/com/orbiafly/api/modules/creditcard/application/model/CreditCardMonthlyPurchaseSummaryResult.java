package com.orbiafly.api.modules.creditcard.application.model;

import java.math.BigDecimal;

public record CreditCardMonthlyPurchaseSummaryResult(
    BigDecimal total,
    BigDecimal paidTotal,
    BigDecimal pendingTotal,
    int totalCount,
    int paidCount,
    int pendingCount,
    BigDecimal paidPercentage) {
}

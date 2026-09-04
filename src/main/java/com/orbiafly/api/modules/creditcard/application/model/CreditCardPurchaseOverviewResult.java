package com.orbiafly.api.modules.creditcard.application.model;

import java.math.BigDecimal;
import java.util.List;

public record CreditCardPurchaseOverviewResult(
    BigDecimal totalCommitted,
    int activePurchaseCount,
    int futureInstallmentCount,
    BigDecimal currentMonthInstallmentsAmount,
    List<CreditCardPurchaseResult> purchases) {
}

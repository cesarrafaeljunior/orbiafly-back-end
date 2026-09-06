package com.orbiafly.api.modules.creditcard.application.model;

import java.util.List;

public record CreditCardMonthlyPurchasesResult(
    int year,
    int month,
    CreditCardMonthlyPurchaseSummaryResult summary,
    List<CreditCardMonthlyPurchaseInstallmentResult> items) {
}

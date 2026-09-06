package com.orbiafly.api.modules.creditcard.web.dto.response;

import java.util.List;

public record CreditCardMonthlyPurchasesResponseDTO(
    int year,
    int month,
    CreditCardMonthlyPurchaseSummaryResponseDTO summary,
    List<CreditCardMonthlyPurchaseInstallmentResponseDTO> items) {
}

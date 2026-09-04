package com.orbiafly.api.modules.creditcard.web.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record CreditCardPurchaseOverviewResponseDTO(
    BigDecimal totalCommitted,
    int activePurchaseCount,
    int futureInstallmentCount,
    BigDecimal currentMonthInstallmentsAmount,
    List<CreditCardPurchaseResponseDTO> purchases) {
}

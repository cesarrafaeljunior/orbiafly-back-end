package com.orbiafly.api.modules.creditcard.application.model;

import java.math.BigDecimal;
import java.util.List;

public record CreditCardOverviewResult(
    BigDecimal totalLimit,
    BigDecimal usedLimit,
    BigDecimal availableLimit,
    int activeCount,
    List<CreditCardResult> cards) {
}

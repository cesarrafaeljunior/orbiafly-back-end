package com.orbiafly.api.modules.financialaccount.application.model;

import java.math.BigDecimal;
import java.util.List;

public record FinancialAccountsOverviewResult(
    BigDecimal totalAvailableBalance,
    int activeCount,
    List<FinancialAccountResult> accounts) {
}

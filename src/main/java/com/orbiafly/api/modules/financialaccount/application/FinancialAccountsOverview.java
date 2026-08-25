package com.orbiafly.api.modules.financialaccount.application;

import java.math.BigDecimal;
import java.util.List;

public record FinancialAccountsOverview(
    BigDecimal totalAvailableBalance,
    int activeCount,
    List<FinancialAccountView> accounts) {
}

package com.orbiafly.api.modules.fixedexpense.application.model;

import java.math.BigDecimal;

public record MonthlyExpenseSummary(
    BigDecimal total,
    BigDecimal paidTotal,
    BigDecimal pendingTotal,
    int paidCount,
    int pendingCount) {
}

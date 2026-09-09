package com.orbiafly.api.modules.expectedincome.application.model;

import java.math.BigDecimal;
import java.util.List;

public record ExpectedIncomeOverviewResult(
    BigDecimal pendingTotal,
    BigDecimal receivedTotal,
    BigDecimal canceledTotal,
    int pendingCount,
    int receivedCount,
    int canceledCount,
    List<ExpectedIncomeResult> items) {
}

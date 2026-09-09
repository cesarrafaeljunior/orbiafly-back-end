package com.orbiafly.api.modules.financialprojection.application.model;

import com.orbiafly.api.modules.financialprojection.domain.FinancialHealthLevel;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record MonthlyFinancialProjectionResult(
    int year,
    int month,
    BigDecimal currentBalance,
    BigDecimal expectedIncomeTotal,
    BigDecimal expectedExpenseTotal,
    BigDecimal projectedFinalBalance,
    BigDecimal minimumProjectedBalance,
    LocalDate minimumProjectedBalanceDate,
    FinancialHealthLevel healthLevel,
    String insight,
    List<ProjectionEventResult> events) {
}

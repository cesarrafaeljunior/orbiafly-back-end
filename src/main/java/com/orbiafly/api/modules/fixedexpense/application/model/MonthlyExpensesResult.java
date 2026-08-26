package com.orbiafly.api.modules.fixedexpense.application.model;

import java.util.List;

public record MonthlyExpensesResult(
    int year,
    int month,
    MonthlyExpenseSummary summary,
    List<MonthlyExpenseResult> items) {
}

package com.orbiafly.api.modules.fixedexpense.application;

import java.util.List;

public record MonthlyExpensesView(
    int year,
    int month,
    MonthlyExpenseSummary summary,
    List<MonthlyExpenseView> items) {
}

package com.orbiafly.api.modules.oneoffexpense.application.model;

import com.orbiafly.api.modules.fixedexpense.application.model.MonthlyExpenseSummary;
import java.util.List;

public record MonthlyOneOffExpensesResult(
    int year,
    int month,
    MonthlyExpenseSummary summary,
    List<OneOffExpenseResult> items) {
}

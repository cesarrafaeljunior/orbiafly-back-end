package com.orbiafly.api.modules.fixedexpense.web.dto.response;

import java.util.List;

public record MonthlyExpensesResponseDTO(
    int year,
    int month,
    MonthlyExpenseSummaryResponseDTO summary,
    List<MonthlyExpenseResponseDTO> items) {
}

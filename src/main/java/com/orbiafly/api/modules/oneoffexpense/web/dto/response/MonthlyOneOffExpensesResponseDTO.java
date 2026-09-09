package com.orbiafly.api.modules.oneoffexpense.web.dto.response;

import com.orbiafly.api.modules.fixedexpense.web.dto.response.MonthlyExpenseSummaryResponseDTO;
import java.util.List;

public record MonthlyOneOffExpensesResponseDTO(
    int year,
    int month,
    MonthlyExpenseSummaryResponseDTO summary,
    List<OneOffExpenseResponseDTO> items) {
}

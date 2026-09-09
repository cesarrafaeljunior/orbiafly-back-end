package com.orbiafly.api.modules.financialprojection.web.dto.response;

import com.orbiafly.api.modules.financialprojection.domain.FinancialHealthLevel;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record MonthlyFinancialProjectionResponseDTO(
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
    List<ProjectionEventResponseDTO> events) {
}

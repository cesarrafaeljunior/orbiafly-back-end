package com.orbiafly.api.modules.fixedexpense.web.dto.response;

import java.math.BigDecimal;

public record MonthlyExpenseSummaryResponseDTO(
    BigDecimal total,
    BigDecimal paidTotal,
    BigDecimal pendingTotal,
    int totalCount,
    int paidCount,
    int pendingCount,
    BigDecimal paidPercentage) {
}

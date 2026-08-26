package com.orbiafly.api.modules.financialaccount.web.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record FinancialAccountsOverviewResponseDTO(
    BigDecimal totalAvailableBalance,
    int activeCount,
    List<FinancialAccountResponseDTO> accounts) {
}

package com.orbiafly.api.modules.expectedincome.web.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record ExpectedIncomeOverviewResponseDTO(
    BigDecimal pendingTotal,
    BigDecimal receivedTotal,
    BigDecimal canceledTotal,
    int pendingCount,
    int receivedCount,
    int canceledCount,
    List<ExpectedIncomeResponseDTO> items) {
}

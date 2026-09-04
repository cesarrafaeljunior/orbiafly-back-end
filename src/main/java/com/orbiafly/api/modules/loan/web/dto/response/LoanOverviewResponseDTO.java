package com.orbiafly.api.modules.loan.web.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record LoanOverviewResponseDTO(
    BigDecimal totalBorrowedRemaining,
    BigDecimal totalLentRemaining,
    int activeLoanCount,
    int upcomingEntryCount,
    List<LoanResponseDTO> loans) {
}

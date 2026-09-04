package com.orbiafly.api.modules.loan.application.model;

import java.math.BigDecimal;
import java.util.List;

public record LoanOverviewResult(
    BigDecimal totalBorrowedRemaining,
    BigDecimal totalLentRemaining,
    int activeLoanCount,
    int upcomingEntryCount,
    List<LoanResult> loans) {
}

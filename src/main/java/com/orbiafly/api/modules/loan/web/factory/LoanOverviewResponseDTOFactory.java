package com.orbiafly.api.modules.loan.web.factory;

import com.orbiafly.api.modules.loan.application.model.LoanOverviewResult;
import com.orbiafly.api.modules.loan.web.dto.response.LoanOverviewResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class LoanOverviewResponseDTOFactory {

  private final LoanResponseDTOFactory loanFactory;

  public LoanOverviewResponseDTOFactory(LoanResponseDTOFactory loanFactory) {
    this.loanFactory = loanFactory;
  }

  public LoanOverviewResponseDTO createLoanOverviewResponseDTO(LoanOverviewResult overview) {
    return new LoanOverviewResponseDTO(
        overview.totalBorrowedRemaining(),
        overview.totalLentRemaining(),
        overview.activeLoanCount(),
        overview.upcomingEntryCount(),
        overview.loans().stream()
            .map(loanFactory::createLoanResponseDTO)
            .toList());
  }
}

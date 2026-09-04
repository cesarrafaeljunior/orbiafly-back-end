package com.orbiafly.api.modules.loan.web.factory;

import com.orbiafly.api.modules.loan.application.model.LoanEntryResult;
import com.orbiafly.api.modules.loan.web.dto.response.LoanEntryResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class LoanEntryResponseDTOFactory {

  public LoanEntryResponseDTO createLoanEntryResponseDTO(LoanEntryResult entry) {
    return new LoanEntryResponseDTO(
        entry.id(),
        entry.installmentId(),
        entry.installmentNumber(),
        entry.amount(),
        entry.occurredOn(),
        entry.financialAccountId(),
        entry.financialAccountName(),
        entry.financialTransactionId(),
        entry.createdAt());
  }
}

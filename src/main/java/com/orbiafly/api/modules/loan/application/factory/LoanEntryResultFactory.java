package com.orbiafly.api.modules.loan.application.factory;

import com.orbiafly.api.modules.loan.application.model.LoanEntryResult;
import com.orbiafly.api.modules.loan.domain.LoanEntry;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class LoanEntryResultFactory {

  public LoanEntryResult createLoanEntryResult(
      LoanEntry entry,
      Map<UUID, String> financialAccountNames) {
    return new LoanEntryResult(
        entry.getId(),
        entry.getInstallment() == null ? null : entry.getInstallment().getId(),
        entry.getInstallment() == null ? null : entry.getInstallment().getInstallmentNumber(),
        entry.getAmount(),
        entry.getOccurredOn(),
        entry.getFinancialAccountId(),
        financialAccountNames.get(entry.getFinancialAccountId()),
        entry.getFinancialTransactionId(),
        entry.getCreatedAt());
  }
}

package com.orbiafly.api.modules.loan.application.factory;

import com.orbiafly.api.modules.loan.application.model.LoanInstallmentResult;
import com.orbiafly.api.modules.loan.domain.LoanInstallment;
import org.springframework.stereotype.Component;

@Component
public class LoanInstallmentResultFactory {

  public LoanInstallmentResult createLoanInstallmentResult(LoanInstallment installment) {
    return new LoanInstallmentResult(
        installment.getId(),
        installment.getInstallmentNumber(),
        installment.getAmount(),
        installment.getDueDate(),
        installment.getStatus(),
        installment.getCompletedAt(),
        installment.getCreatedAt(),
        installment.getUpdatedAt());
  }
}

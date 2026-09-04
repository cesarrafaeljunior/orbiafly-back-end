package com.orbiafly.api.modules.loan.web.factory;

import com.orbiafly.api.modules.loan.application.model.LoanInstallmentResult;
import com.orbiafly.api.modules.loan.web.dto.response.LoanInstallmentResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class LoanInstallmentResponseDTOFactory {

  public LoanInstallmentResponseDTO createLoanInstallmentResponseDTO(
      LoanInstallmentResult installment) {
    if (installment == null) {
      return null;
    }
    return new LoanInstallmentResponseDTO(
        installment.id(),
        installment.installmentNumber(),
        installment.amount(),
        installment.dueDate(),
        installment.status(),
        installment.completedAt(),
        installment.createdAt(),
        installment.updatedAt());
  }
}

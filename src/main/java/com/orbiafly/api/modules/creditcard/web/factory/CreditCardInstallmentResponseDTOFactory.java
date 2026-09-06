package com.orbiafly.api.modules.creditcard.web.factory;

import com.orbiafly.api.modules.creditcard.application.model.CreditCardInstallmentResult;
import com.orbiafly.api.modules.creditcard.web.dto.response.CreditCardInstallmentResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class CreditCardInstallmentResponseDTOFactory {

  public CreditCardInstallmentResponseDTO createCreditCardInstallmentResponseDTO(
      CreditCardInstallmentResult installment) {
    return new CreditCardInstallmentResponseDTO(
        installment.id(),
        installment.purchaseId(),
        installment.installmentNumber(),
        installment.amount(),
        installment.competenceYear(),
        installment.competenceMonth(),
        installment.dueDate(),
        installment.status(),
        installment.paidAt(),
        installment.createdAt(),
        installment.updatedAt());
  }
}

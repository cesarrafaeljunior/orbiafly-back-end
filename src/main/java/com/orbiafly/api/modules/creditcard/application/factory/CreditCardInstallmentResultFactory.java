package com.orbiafly.api.modules.creditcard.application.factory;

import com.orbiafly.api.modules.creditcard.application.model.CreditCardInstallmentResult;
import com.orbiafly.api.modules.creditcard.domain.CreditCardInstallment;
import org.springframework.stereotype.Component;

@Component
public class CreditCardInstallmentResultFactory {

  public CreditCardInstallmentResult createCreditCardInstallmentResult(
      CreditCardInstallment installment) {
    return new CreditCardInstallmentResult(
        installment.getId(),
        installment.getPurchase().getId(),
        installment.getInstallmentNumber(),
        installment.getAmount(),
        installment.getCompetenceYear(),
        installment.getCompetenceMonth(),
        installment.getDueDate(),
        installment.getStatus(),
        installment.getPaidAt(),
        installment.getCreatedAt(),
        installment.getUpdatedAt());
  }
}

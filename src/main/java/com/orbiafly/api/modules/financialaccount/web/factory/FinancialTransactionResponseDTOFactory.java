package com.orbiafly.api.modules.financialaccount.web.factory;

import com.orbiafly.api.modules.financialaccount.application.model.FinancialTransactionResult;
import com.orbiafly.api.modules.financialaccount.web.dto.response.FinancialTransactionResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class FinancialTransactionResponseDTOFactory {

  public FinancialTransactionResponseDTO createFinancialTransactionResponseDTO(
      FinancialTransactionResult transaction) {
    return new FinancialTransactionResponseDTO(
        transaction.id(),
        transaction.financialAccountId(),
        transaction.type(),
        transaction.amount(),
        transaction.balanceEffect(),
        transaction.description(),
        transaction.occurredOn(),
        transaction.referenceType(),
        transaction.referenceId(),
        transaction.reversalOfId(),
        transaction.createdAt());
  }
}

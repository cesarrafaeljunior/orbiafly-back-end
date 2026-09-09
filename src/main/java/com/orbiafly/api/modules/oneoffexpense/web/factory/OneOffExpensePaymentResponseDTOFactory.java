package com.orbiafly.api.modules.oneoffexpense.web.factory;

import com.orbiafly.api.modules.financialaccount.web.factory.FinancialAccountResponseDTOFactory;
import com.orbiafly.api.modules.financialaccount.web.factory.FinancialTransactionResponseDTOFactory;
import com.orbiafly.api.modules.oneoffexpense.application.model.OneOffExpensePaymentResult;
import com.orbiafly.api.modules.oneoffexpense.web.dto.response.OneOffExpensePaymentResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class OneOffExpensePaymentResponseDTOFactory {

  private final OneOffExpenseResponseDTOFactory expenseFactory;
  private final FinancialTransactionResponseDTOFactory transactionFactory;
  private final FinancialAccountResponseDTOFactory accountFactory;

  public OneOffExpensePaymentResponseDTOFactory(
      OneOffExpenseResponseDTOFactory expenseFactory,
      FinancialTransactionResponseDTOFactory transactionFactory,
      FinancialAccountResponseDTOFactory accountFactory) {
    this.expenseFactory = expenseFactory;
    this.transactionFactory = transactionFactory;
    this.accountFactory = accountFactory;
  }

  public OneOffExpensePaymentResponseDTO createOneOffExpensePaymentResponseDTO(
      OneOffExpensePaymentResult result) {
    return new OneOffExpensePaymentResponseDTO(
        expenseFactory.createOneOffExpenseResponseDTO(result.expense()),
        transactionFactory.createFinancialTransactionResponseDTO(result.payment()),
        accountFactory.createFinancialAccountResponseDTO(result.financialAccount()),
        result.updatedBalance(),
        result.negativeBalance());
  }
}

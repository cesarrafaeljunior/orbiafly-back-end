package com.orbiafly.api.modules.expectedincome.web.factory;

import com.orbiafly.api.modules.expectedincome.application.model.ExpectedIncomeReceiptResult;
import com.orbiafly.api.modules.expectedincome.web.dto.response.ExpectedIncomeReceiptResponseDTO;
import com.orbiafly.api.modules.financialaccount.web.factory.AccountTransactionResponseDTOFactory;
import org.springframework.stereotype.Component;

@Component
public class ExpectedIncomeReceiptResponseDTOFactory {

  private final ExpectedIncomeResponseDTOFactory incomeFactory;
  private final AccountTransactionResponseDTOFactory transactionFactory;

  public ExpectedIncomeReceiptResponseDTOFactory(
      ExpectedIncomeResponseDTOFactory incomeFactory,
      AccountTransactionResponseDTOFactory transactionFactory) {
    this.incomeFactory = incomeFactory;
    this.transactionFactory = transactionFactory;
  }

  public ExpectedIncomeReceiptResponseDTO createExpectedIncomeReceiptResponseDTO(
      ExpectedIncomeReceiptResult result) {
    return new ExpectedIncomeReceiptResponseDTO(
        incomeFactory.createExpectedIncomeResponseDTO(result.income()),
        transactionFactory.createAccountTransactionResponseDTO(result.receipt()));
  }
}

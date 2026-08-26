package com.orbiafly.api.modules.financialaccount.web.factory;

import com.orbiafly.api.modules.financialaccount.application.model.AccountTransactionResult;
import com.orbiafly.api.modules.financialaccount.web.dto.response.AccountTransactionResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class AccountTransactionResponseDTOFactory {

  private final FinancialAccountResponseDTOFactory accountFactory;
  private final FinancialTransactionResponseDTOFactory transactionFactory;

  public AccountTransactionResponseDTOFactory(
      FinancialAccountResponseDTOFactory accountFactory,
      FinancialTransactionResponseDTOFactory transactionFactory) {
    this.accountFactory = accountFactory;
    this.transactionFactory = transactionFactory;
  }

  public AccountTransactionResponseDTO createAccountTransactionResponseDTO(
      AccountTransactionResult result) {
    return new AccountTransactionResponseDTO(
        accountFactory.createFinancialAccountResponseDTO(result.financialAccount()),
        transactionFactory.createFinancialTransactionResponseDTO(result.transaction()));
  }
}

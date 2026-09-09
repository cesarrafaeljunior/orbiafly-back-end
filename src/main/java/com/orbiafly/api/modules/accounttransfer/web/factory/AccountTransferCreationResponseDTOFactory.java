package com.orbiafly.api.modules.accounttransfer.web.factory;

import com.orbiafly.api.modules.accounttransfer.application.model.AccountTransferCreationResult;
import com.orbiafly.api.modules.accounttransfer.web.dto.response.AccountTransferCreationResponseDTO;
import com.orbiafly.api.modules.financialaccount.web.factory.AccountTransactionResponseDTOFactory;
import org.springframework.stereotype.Component;

@Component
public class AccountTransferCreationResponseDTOFactory {

  private final AccountTransferResponseDTOFactory transferFactory;
  private final AccountTransactionResponseDTOFactory transactionFactory;

  public AccountTransferCreationResponseDTOFactory(
      AccountTransferResponseDTOFactory transferFactory,
      AccountTransactionResponseDTOFactory transactionFactory) {
    this.transferFactory = transferFactory;
    this.transactionFactory = transactionFactory;
  }

  public AccountTransferCreationResponseDTO createAccountTransferCreationResponseDTO(
      AccountTransferCreationResult result) {
    return new AccountTransferCreationResponseDTO(
        transferFactory.createAccountTransferResponseDTO(result.transfer()),
        transactionFactory.createAccountTransactionResponseDTO(result.source()),
        transactionFactory.createAccountTransactionResponseDTO(result.destination()));
  }
}

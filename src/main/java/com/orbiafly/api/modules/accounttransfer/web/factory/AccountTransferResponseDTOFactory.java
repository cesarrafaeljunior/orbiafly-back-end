package com.orbiafly.api.modules.accounttransfer.web.factory;

import com.orbiafly.api.modules.accounttransfer.application.model.AccountTransferResult;
import com.orbiafly.api.modules.accounttransfer.web.dto.response.AccountTransferResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class AccountTransferResponseDTOFactory {

  public AccountTransferResponseDTO createAccountTransferResponseDTO(AccountTransferResult result) {
    return new AccountTransferResponseDTO(
        result.id(),
        result.sourceAccountId(),
        result.sourceAccountName(),
        result.destinationAccountId(),
        result.destinationAccountName(),
        result.amount(),
        result.transferDate(),
        result.description(),
        result.sourceTransactionId(),
        result.destinationTransactionId(),
        result.createdAt(),
        result.updatedAt());
  }
}

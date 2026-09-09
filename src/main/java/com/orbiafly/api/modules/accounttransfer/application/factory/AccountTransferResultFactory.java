package com.orbiafly.api.modules.accounttransfer.application.factory;

import com.orbiafly.api.modules.accounttransfer.application.model.AccountTransferResult;
import com.orbiafly.api.modules.accounttransfer.domain.AccountTransfer;
import org.springframework.stereotype.Component;

@Component
public class AccountTransferResultFactory {

  public AccountTransferResult createAccountTransferResult(AccountTransfer transfer) {
    return new AccountTransferResult(
        transfer.getId(),
        transfer.getSourceAccount().getId(),
        transfer.getSourceAccount().getName(),
        transfer.getDestinationAccount().getId(),
        transfer.getDestinationAccount().getName(),
        transfer.getAmount(),
        transfer.getTransferDate(),
        transfer.getDescription(),
        transfer.getSourceTransactionId(),
        transfer.getDestinationTransactionId(),
        transfer.getCreatedAt(),
        transfer.getUpdatedAt());
  }
}

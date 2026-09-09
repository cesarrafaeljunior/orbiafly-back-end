package com.orbiafly.api.modules.accounttransfer.web.factory;

import com.orbiafly.api.modules.accounttransfer.application.model.AccountTransferOverviewResult;
import com.orbiafly.api.modules.accounttransfer.web.dto.response.AccountTransferOverviewResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class AccountTransferOverviewResponseDTOFactory {

  private final AccountTransferResponseDTOFactory transferFactory;

  public AccountTransferOverviewResponseDTOFactory(AccountTransferResponseDTOFactory transferFactory) {
    this.transferFactory = transferFactory;
  }

  public AccountTransferOverviewResponseDTO createAccountTransferOverviewResponseDTO(
      AccountTransferOverviewResult result) {
    return new AccountTransferOverviewResponseDTO(
        result.transferredTotal(),
        result.transferCount(),
        result.items().stream().map(transferFactory::createAccountTransferResponseDTO).toList());
  }
}

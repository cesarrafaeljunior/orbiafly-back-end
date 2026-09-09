package com.orbiafly.api.modules.accounttransfer.application.contract;

import com.orbiafly.api.modules.accounttransfer.web.dto.response.AccountTransferCreationResponseDTO;
import com.orbiafly.api.modules.accounttransfer.web.dto.response.AccountTransferOverviewResponseDTO;
import com.orbiafly.api.modules.accounttransfer.web.dto.response.AccountTransferResponseDTO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface AccountTransferServiceInterface {

  AccountTransferOverviewResponseDTO listAccountTransfers();

  AccountTransferResponseDTO getAccountTransfer(UUID id);

  AccountTransferCreationResponseDTO createAccountTransfer(
      UUID sourceAccountId,
      UUID destinationAccountId,
      BigDecimal amount,
      LocalDate transferDate,
      String description);
}

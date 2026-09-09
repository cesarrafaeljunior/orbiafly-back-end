package com.orbiafly.api.modules.accounttransfer.web.dto.response;

import com.orbiafly.api.modules.financialaccount.web.dto.response.AccountTransactionResponseDTO;

public record AccountTransferCreationResponseDTO(
    AccountTransferResponseDTO transfer,
    AccountTransactionResponseDTO source,
    AccountTransactionResponseDTO destination) {
}

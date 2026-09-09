package com.orbiafly.api.modules.accounttransfer.application.model;

import com.orbiafly.api.modules.financialaccount.application.model.AccountTransactionResult;

public record AccountTransferCreationResult(
    AccountTransferResult transfer,
    AccountTransactionResult source,
    AccountTransactionResult destination) {
}

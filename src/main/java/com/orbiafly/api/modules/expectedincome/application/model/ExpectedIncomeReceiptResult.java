package com.orbiafly.api.modules.expectedincome.application.model;

import com.orbiafly.api.modules.financialaccount.application.model.AccountTransactionResult;

public record ExpectedIncomeReceiptResult(
    ExpectedIncomeResult income,
    AccountTransactionResult receipt) {
}

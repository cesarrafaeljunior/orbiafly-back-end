package com.orbiafly.api.modules.financialaccount.application;

public record AccountTransactionResult(
    FinancialAccountView financialAccount,
    FinancialTransactionView transaction) {
}

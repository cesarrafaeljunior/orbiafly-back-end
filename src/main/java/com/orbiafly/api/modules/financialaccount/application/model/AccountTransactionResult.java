package com.orbiafly.api.modules.financialaccount.application.model;

public record AccountTransactionResult(
    FinancialAccountResult financialAccount,
    FinancialTransactionResult transaction) {
}

package com.orbiafly.api.modules.installmentexpense.application.model;

import com.orbiafly.api.modules.financialaccount.application.model.FinancialAccountResult;
import com.orbiafly.api.modules.financialaccount.application.model.FinancialTransactionResult;

public record InstallmentPaymentResult(
    InstallmentResult installment,
    FinancialTransactionResult transaction,
    FinancialAccountResult financialAccount) {
}

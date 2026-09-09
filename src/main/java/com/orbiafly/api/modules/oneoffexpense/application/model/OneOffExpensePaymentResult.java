package com.orbiafly.api.modules.oneoffexpense.application.model;

import com.orbiafly.api.modules.financialaccount.application.model.FinancialAccountResult;
import com.orbiafly.api.modules.financialaccount.application.model.FinancialTransactionResult;
import java.math.BigDecimal;

public record OneOffExpensePaymentResult(
    OneOffExpenseResult expense,
    FinancialTransactionResult payment,
    FinancialAccountResult financialAccount,
    BigDecimal updatedBalance,
    boolean negativeBalance) {
}

package com.orbiafly.api.modules.fixedexpense.application.model;

import com.orbiafly.api.modules.financialaccount.application.model.FinancialAccountResult;
import com.orbiafly.api.modules.financialaccount.application.model.FinancialTransactionResult;
import java.math.BigDecimal;

public record FixedExpensePaymentResult(
    MonthlyExpenseResult occurrence,
    FinancialTransactionResult payment,
    FinancialAccountResult financialAccount,
    BigDecimal updatedBalance,
    boolean negativeBalance) {
}

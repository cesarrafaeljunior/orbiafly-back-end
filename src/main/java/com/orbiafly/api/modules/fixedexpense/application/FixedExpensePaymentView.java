package com.orbiafly.api.modules.fixedexpense.application;

import com.orbiafly.api.modules.financialaccount.application.FinancialAccountView;
import com.orbiafly.api.modules.financialaccount.application.FinancialTransactionView;
import java.math.BigDecimal;

public record FixedExpensePaymentView(
    MonthlyExpenseView occurrence,
    FinancialTransactionView payment,
    FinancialAccountView financialAccount,
    BigDecimal updatedBalance,
    boolean negativeBalance) {
}

package com.orbiafly.api.modules.fixedexpense.web.dto.response;

import com.orbiafly.api.modules.financialaccount.web.dto.response.FinancialAccountResponseDTO;
import com.orbiafly.api.modules.financialaccount.web.dto.response.FinancialTransactionResponseDTO;
import java.math.BigDecimal;

public record FixedExpensePaymentResponseDTO(
    MonthlyExpenseResponseDTO occurrence,
    FinancialTransactionResponseDTO payment,
    FinancialAccountResponseDTO financialAccount,
    BigDecimal updatedBalance,
    boolean negativeBalance) {
}

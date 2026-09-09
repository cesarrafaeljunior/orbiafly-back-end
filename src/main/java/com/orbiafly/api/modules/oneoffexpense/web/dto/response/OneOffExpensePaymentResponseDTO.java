package com.orbiafly.api.modules.oneoffexpense.web.dto.response;

import com.orbiafly.api.modules.financialaccount.web.dto.response.FinancialAccountResponseDTO;
import com.orbiafly.api.modules.financialaccount.web.dto.response.FinancialTransactionResponseDTO;
import java.math.BigDecimal;

public record OneOffExpensePaymentResponseDTO(
    OneOffExpenseResponseDTO expense,
    FinancialTransactionResponseDTO payment,
    FinancialAccountResponseDTO financialAccount,
    BigDecimal updatedBalance,
    boolean negativeBalance) {
}

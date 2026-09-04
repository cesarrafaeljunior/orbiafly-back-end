package com.orbiafly.api.modules.installmentexpense.web.dto.response;

import com.orbiafly.api.modules.financialaccount.web.dto.response.FinancialAccountResponseDTO;
import com.orbiafly.api.modules.financialaccount.web.dto.response.FinancialTransactionResponseDTO;

public record InstallmentPaymentResponseDTO(
    InstallmentResponseDTO installment,
    FinancialTransactionResponseDTO transaction,
    FinancialAccountResponseDTO financialAccount) {
}

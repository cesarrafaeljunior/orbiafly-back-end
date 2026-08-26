package com.orbiafly.api.modules.financialaccount.web.dto.response;

public record AccountTransactionResponseDTO(
    FinancialAccountResponseDTO financialAccount,
    FinancialTransactionResponseDTO transaction) {
}

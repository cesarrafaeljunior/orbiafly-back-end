package com.orbiafly.api.modules.expectedincome.web.dto.response;

import com.orbiafly.api.modules.financialaccount.web.dto.response.AccountTransactionResponseDTO;

public record ExpectedIncomeReceiptResponseDTO(
    ExpectedIncomeResponseDTO income,
    AccountTransactionResponseDTO receipt) {
}

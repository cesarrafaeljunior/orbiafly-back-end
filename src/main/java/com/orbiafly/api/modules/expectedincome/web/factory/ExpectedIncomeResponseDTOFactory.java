package com.orbiafly.api.modules.expectedincome.web.factory;

import com.orbiafly.api.modules.expectedincome.application.model.ExpectedIncomeResult;
import com.orbiafly.api.modules.expectedincome.web.dto.response.ExpectedIncomeResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class ExpectedIncomeResponseDTOFactory {

  public ExpectedIncomeResponseDTO createExpectedIncomeResponseDTO(ExpectedIncomeResult result) {
    return new ExpectedIncomeResponseDTO(
        result.id(),
        result.description(),
        result.amount(),
        result.expectedDate(),
        result.financialAccountId(),
        result.financialAccountName(),
        result.status(),
        result.receivedAt(),
        result.financialTransactionId(),
        result.notes(),
        result.createdAt(),
        result.updatedAt());
  }
}

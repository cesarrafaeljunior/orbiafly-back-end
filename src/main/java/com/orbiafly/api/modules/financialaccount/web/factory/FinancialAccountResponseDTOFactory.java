package com.orbiafly.api.modules.financialaccount.web.factory;

import com.orbiafly.api.modules.financialaccount.application.model.FinancialAccountResult;
import com.orbiafly.api.modules.financialaccount.web.dto.response.FinancialAccountResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class FinancialAccountResponseDTOFactory {

  public FinancialAccountResponseDTO createFinancialAccountResponseDTO(
      FinancialAccountResult account) {
    return new FinancialAccountResponseDTO(
        account.id(),
        account.name(),
        account.type(),
        account.institution(),
        account.openingBalance(),
        account.currentBalance(),
        account.active(),
        account.createdAt(),
        account.updatedAt());
  }
}

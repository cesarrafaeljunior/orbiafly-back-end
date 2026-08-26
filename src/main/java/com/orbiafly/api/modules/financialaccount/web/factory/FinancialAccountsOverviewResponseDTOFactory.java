package com.orbiafly.api.modules.financialaccount.web.factory;

import com.orbiafly.api.modules.financialaccount.application.model.FinancialAccountsOverviewResult;
import com.orbiafly.api.modules.financialaccount.web.dto.response.FinancialAccountsOverviewResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class FinancialAccountsOverviewResponseDTOFactory {

  private final FinancialAccountResponseDTOFactory accountFactory;

  public FinancialAccountsOverviewResponseDTOFactory(
      FinancialAccountResponseDTOFactory accountFactory) {
    this.accountFactory = accountFactory;
  }

  public FinancialAccountsOverviewResponseDTO createFinancialAccountsOverviewResponseDTO(
      FinancialAccountsOverviewResult overview) {
    return new FinancialAccountsOverviewResponseDTO(
        overview.totalAvailableBalance(),
        overview.activeCount(),
        overview.accounts().stream()
            .map(accountFactory::createFinancialAccountResponseDTO)
            .toList());
  }
}

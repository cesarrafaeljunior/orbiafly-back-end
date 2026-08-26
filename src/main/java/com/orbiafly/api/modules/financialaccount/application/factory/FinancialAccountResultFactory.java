package com.orbiafly.api.modules.financialaccount.application.factory;

import com.orbiafly.api.modules.financialaccount.application.model.FinancialAccountResult;
import com.orbiafly.api.modules.financialaccount.domain.FinancialAccount;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class FinancialAccountResultFactory {

  public FinancialAccountResult createFinancialAccountResult(
      FinancialAccount account,
      BigDecimal currentBalance) {
    return new FinancialAccountResult(
        account.getId(),
        account.getName(),
        account.getType(),
        account.getInstitution(),
        account.getOpeningBalance(),
        currentBalance,
        account.isActive(),
        account.getCreatedAt(),
        account.getUpdatedAt());
  }
}

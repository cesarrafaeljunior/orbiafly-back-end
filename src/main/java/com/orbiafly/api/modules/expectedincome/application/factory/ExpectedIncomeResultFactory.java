package com.orbiafly.api.modules.expectedincome.application.factory;

import com.orbiafly.api.modules.expectedincome.application.model.ExpectedIncomeResult;
import com.orbiafly.api.modules.expectedincome.domain.ExpectedIncome;
import com.orbiafly.api.modules.financialaccount.domain.FinancialAccount;
import org.springframework.stereotype.Component;

@Component
public class ExpectedIncomeResultFactory {

  public ExpectedIncomeResult createExpectedIncomeResult(ExpectedIncome income) {
    FinancialAccount account = income.getFinancialAccount();
    return new ExpectedIncomeResult(
        income.getId(),
        income.getDescription(),
        income.getAmount(),
        income.getExpectedDate(),
        account == null ? null : account.getId(),
        account == null ? null : account.getName(),
        income.getStatus(),
        income.getReceivedAt(),
        income.getFinancialTransactionId(),
        income.getNotes(),
        income.getCreatedAt(),
        income.getUpdatedAt());
  }
}

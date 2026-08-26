package com.orbiafly.api.modules.fixedexpense.application.factory;

import com.orbiafly.api.modules.fixedexpense.application.model.MonthlyExpenseResult;
import com.orbiafly.api.modules.fixedexpense.domain.FixedExpense;
import com.orbiafly.api.modules.fixedexpense.domain.FixedExpenseOccurrence;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class MonthlyExpenseResultFactory {

  public MonthlyExpenseResult createMonthlyExpenseResult(
      FixedExpenseOccurrence occurrence,
      Map<UUID, String> accountNames) {
    FixedExpense expense = occurrence.getFixedExpense();
    String accountName = occurrence.getFinancialAccountId() == null
        ? null
        : accountNames.get(occurrence.getFinancialAccountId());
    return new MonthlyExpenseResult(
        occurrence.getId(),
        expense.getId(),
        occurrence.getName(),
        occurrence.getAmount(),
        expense.getDefaultAmount(),
        occurrence.getDueDay(),
        occurrence.getCategoryId(),
        occurrence.getCategoryName(),
        occurrence.getStatus(),
        occurrence.getPaidAt(),
        occurrence.getFinancialAccountId(),
        accountName,
        expense.isActive());
  }
}

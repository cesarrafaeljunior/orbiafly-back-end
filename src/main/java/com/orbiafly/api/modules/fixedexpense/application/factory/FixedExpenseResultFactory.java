package com.orbiafly.api.modules.fixedexpense.application.factory;

import com.orbiafly.api.modules.fixedexpense.application.model.FixedExpenseResult;
import com.orbiafly.api.modules.fixedexpense.domain.FixedExpense;
import org.springframework.stereotype.Component;

@Component
public class FixedExpenseResultFactory {

  public FixedExpenseResult createFixedExpenseResult(FixedExpense expense) {
    return new FixedExpenseResult(
        expense.getId(),
        expense.getName(),
        expense.getDefaultAmount(),
        expense.getDueDay(),
        expense.getCategory().getId(),
        expense.getCategory().getName(),
        expense.isActive(),
        expense.getCreatedAt(),
        expense.getUpdatedAt());
  }
}

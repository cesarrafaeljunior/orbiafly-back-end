package com.orbiafly.api.modules.fixedexpense.web.factory;

import com.orbiafly.api.modules.fixedexpense.application.model.MonthlyExpenseResult;
import com.orbiafly.api.modules.fixedexpense.web.dto.response.MonthlyExpenseResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class MonthlyExpenseResponseDTOFactory {

  public MonthlyExpenseResponseDTO createMonthlyExpenseResponseDTO(MonthlyExpenseResult item) {
    return new MonthlyExpenseResponseDTO(
        item.occurrenceId(),
        item.fixedExpenseId(),
        item.name(),
        item.amount(),
        item.defaultAmount(),
        item.dueDay(),
        item.categoryId(),
        item.category(),
        item.status(),
        item.paidAt(),
        item.financialAccountId(),
        item.financialAccountName(),
        item.active());
  }
}

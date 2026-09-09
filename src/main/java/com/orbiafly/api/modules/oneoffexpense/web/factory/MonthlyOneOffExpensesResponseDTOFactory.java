package com.orbiafly.api.modules.oneoffexpense.web.factory;

import com.orbiafly.api.modules.fixedexpense.web.dto.response.MonthlyExpenseSummaryResponseDTO;
import com.orbiafly.api.modules.oneoffexpense.application.model.MonthlyOneOffExpensesResult;
import com.orbiafly.api.modules.oneoffexpense.web.dto.response.MonthlyOneOffExpensesResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class MonthlyOneOffExpensesResponseDTOFactory {

  private final OneOffExpenseResponseDTOFactory expenseFactory;

  public MonthlyOneOffExpensesResponseDTOFactory(OneOffExpenseResponseDTOFactory expenseFactory) {
    this.expenseFactory = expenseFactory;
  }

  public MonthlyOneOffExpensesResponseDTO createMonthlyOneOffExpensesResponseDTO(
      MonthlyOneOffExpensesResult result) {
    return new MonthlyOneOffExpensesResponseDTO(
        result.year(),
        result.month(),
        new MonthlyExpenseSummaryResponseDTO(
            result.summary().total(), result.summary().paidTotal(), result.summary().pendingTotal(),
            result.summary().totalCount(), result.summary().paidCount(), result.summary().pendingCount(),
            result.summary().paidPercentage()),
        result.items().stream().map(expenseFactory::createOneOffExpenseResponseDTO).toList());
  }
}

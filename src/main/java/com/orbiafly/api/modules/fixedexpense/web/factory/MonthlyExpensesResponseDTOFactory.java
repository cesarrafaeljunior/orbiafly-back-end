package com.orbiafly.api.modules.fixedexpense.web.factory;

import com.orbiafly.api.modules.fixedexpense.application.model.MonthlyExpenseSummary;
import com.orbiafly.api.modules.fixedexpense.application.model.MonthlyExpensesResult;
import com.orbiafly.api.modules.fixedexpense.web.dto.response.MonthlyExpenseSummaryResponseDTO;
import com.orbiafly.api.modules.fixedexpense.web.dto.response.MonthlyExpensesResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class MonthlyExpensesResponseDTOFactory {

  private final MonthlyExpenseResponseDTOFactory monthlyExpenseFactory;

  public MonthlyExpensesResponseDTOFactory(
      MonthlyExpenseResponseDTOFactory monthlyExpenseFactory) {
    this.monthlyExpenseFactory = monthlyExpenseFactory;
  }

  public MonthlyExpensesResponseDTO createMonthlyExpensesResponseDTO(MonthlyExpensesResult monthly) {
    return new MonthlyExpensesResponseDTO(
        monthly.year(),
        monthly.month(),
        summaryFrom(monthly.summary()),
        monthly.items().stream()
            .map(monthlyExpenseFactory::createMonthlyExpenseResponseDTO)
            .toList());
  }

  private MonthlyExpenseSummaryResponseDTO summaryFrom(MonthlyExpenseSummary summary) {
    return new MonthlyExpenseSummaryResponseDTO(
        summary.total(),
        summary.paidTotal(),
        summary.pendingTotal(),
        summary.paidCount(),
        summary.pendingCount());
  }
}

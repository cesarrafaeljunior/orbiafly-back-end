package com.orbiafly.api.modules.financialprojection.web.factory;

import com.orbiafly.api.modules.financialprojection.application.model.MonthlyFinancialProjectionResult;
import com.orbiafly.api.modules.financialprojection.application.model.ProjectionEventResult;
import com.orbiafly.api.modules.financialprojection.web.dto.response.MonthlyFinancialProjectionResponseDTO;
import com.orbiafly.api.modules.financialprojection.web.dto.response.ProjectionEventResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class MonthlyFinancialProjectionResponseDTOFactory {

  public MonthlyFinancialProjectionResponseDTO createMonthlyFinancialProjectionResponseDTO(
      MonthlyFinancialProjectionResult result) {
    return new MonthlyFinancialProjectionResponseDTO(
        result.year(),
        result.month(),
        result.currentBalance(),
        result.expectedIncomeTotal(),
        result.expectedExpenseTotal(),
        result.projectedFinalBalance(),
        result.minimumProjectedBalance(),
        result.minimumProjectedBalanceDate(),
        result.healthLevel(),
        result.insight(),
        result.events().stream().map(this::toEvent).toList());
  }

  private ProjectionEventResponseDTO toEvent(ProjectionEventResult event) {
    return new ProjectionEventResponseDTO(
        event.date(),
        event.description(),
        event.amount(),
        event.direction(),
        event.sourceType(),
        event.sourceId(),
        event.projectedBalanceAfterEvent());
  }
}

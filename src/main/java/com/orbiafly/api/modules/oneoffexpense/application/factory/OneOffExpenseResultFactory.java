package com.orbiafly.api.modules.oneoffexpense.application.factory;

import com.orbiafly.api.modules.fixedexpense.domain.PaymentStatus;
import com.orbiafly.api.modules.oneoffexpense.application.model.OneOffExpenseResult;
import com.orbiafly.api.modules.oneoffexpense.domain.OneOffExpense;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class OneOffExpenseResultFactory {

  private final Clock clock;

  public OneOffExpenseResultFactory(Clock clock) {
    this.clock = clock;
  }

  public OneOffExpenseResult createOneOffExpenseResult(
      OneOffExpense expense,
      Map<UUID, String> accountNames) {
    LocalDate today = LocalDate.now(clock);
    int daysDelta = (int) ChronoUnit.DAYS.between(today, expense.getExpenseDate());
    boolean overdue = expense.getStatus() == PaymentStatus.PENDING && daysDelta < 0;
    return new OneOffExpenseResult(
        expense.getId(),
        expense.getName(),
        expense.getAmount(),
        expense.getExpenseDate(),
        expense.getCategory().getId(),
        expense.getCategory().getName(),
        expense.getStatus(),
        expense.getPaidAt(),
        expense.getFinancialAccountId(),
        expense.getFinancialAccountId() == null
            ? null
            : accountNames.get(expense.getFinancialAccountId()),
        overdue,
        daysDelta >= 0 ? daysDelta : null,
        overdue ? Math.abs(daysDelta) : null,
        expense.getCreatedAt(),
        expense.getUpdatedAt());
  }
}

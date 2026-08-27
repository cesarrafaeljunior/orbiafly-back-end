package com.orbiafly.api.modules.fixedexpense.application.factory;

import com.orbiafly.api.modules.fixedexpense.application.model.MonthlyExpenseResult;
import com.orbiafly.api.modules.fixedexpense.domain.FixedExpense;
import com.orbiafly.api.modules.fixedexpense.domain.FixedExpenseOccurrence;
import com.orbiafly.api.modules.fixedexpense.domain.PaymentStatus;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class MonthlyExpenseResultFactory {

  private final Clock clock;

  public MonthlyExpenseResultFactory(Clock clock) {
    this.clock = clock;
  }

  public MonthlyExpenseResult createMonthlyExpenseResult(
      FixedExpenseOccurrence occurrence,
      Map<UUID, String> accountNames) {
    FixedExpense expense = occurrence.getFixedExpense();
    String accountName = occurrence.getFinancialAccountId() == null
        ? null
        : accountNames.get(occurrence.getFinancialAccountId());
    LocalDate dueDate = resolveDueDate(occurrence);
    LocalDate today = LocalDate.now(clock);
    boolean overdue = occurrence.getStatus() == PaymentStatus.PENDING && dueDate.isBefore(today);
    int daysDelta = (int) ChronoUnit.DAYS.between(today, dueDate);
    return new MonthlyExpenseResult(
        occurrence.getId(),
        expense.getId(),
        occurrence.getName(),
        occurrence.getAmount(),
        expense.getDefaultAmount(),
        dueDate,
        occurrence.getDueDay(),
        occurrence.getCategoryId(),
        occurrence.getCategoryName(),
        occurrence.getStatus(),
        occurrence.getPaidAt(),
        occurrence.getFinancialAccountId(),
        accountName,
        overdue,
        daysDelta >= 0 ? daysDelta : null,
        overdue ? Math.abs(daysDelta) : null,
        expense.isActive());
  }

  private LocalDate resolveDueDate(FixedExpenseOccurrence occurrence) {
    YearMonth period = YearMonth.of(occurrence.getYear(), occurrence.getMonth());
    int dueDay = Math.min(occurrence.getDueDay(), period.lengthOfMonth());
    return period.atDay(dueDay);
  }
}

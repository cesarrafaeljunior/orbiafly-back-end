package com.orbiafly.api.modules.dashboard.application;

import com.orbiafly.api.modules.dashboard.application.DashboardMonthlyView.CategoryTotal;
import com.orbiafly.api.modules.dashboard.application.DashboardMonthlyView.Competence;
import com.orbiafly.api.modules.dashboard.application.DashboardMonthlyView.AccountItem;
import com.orbiafly.api.modules.dashboard.application.DashboardMonthlyView.AccountsSummary;
import com.orbiafly.api.modules.dashboard.application.DashboardMonthlyView.ExpenseItem;
import com.orbiafly.api.modules.dashboard.application.DashboardMonthlyView.Overdue;
import com.orbiafly.api.modules.dashboard.application.DashboardMonthlyView.Summary;
import com.orbiafly.api.modules.fixedexpense.application.FixedExpenseService;
import com.orbiafly.api.modules.financialaccount.application.FinancialAccountService;
import com.orbiafly.api.modules.financialaccount.application.FinancialAccountsOverview;
import com.orbiafly.api.modules.fixedexpense.application.MonthlyExpenseSummary;
import com.orbiafly.api.modules.fixedexpense.application.MonthlyExpenseView;
import com.orbiafly.api.modules.fixedexpense.application.MonthlyExpensesView;
import com.orbiafly.api.modules.fixedexpense.domain.PaymentStatus;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

  private static final int LIST_LIMIT = 5;
  private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

  private final FixedExpenseService fixedExpenseService;
  private final FinancialAccountService financialAccountService;
  private final Clock clock;

  public DashboardService(
      FixedExpenseService fixedExpenseService,
      FinancialAccountService financialAccountService,
      Clock clock) {
    this.fixedExpenseService = fixedExpenseService;
    this.financialAccountService = financialAccountService;
    this.clock = clock;
  }

  public DashboardMonthlyView getMonthly(int year, int month) {
    MonthlyExpensesView monthly = fixedExpenseService.getMonthly(year, month);
    FinancialAccountsOverview accountsOverview = financialAccountService.list(false);
    MonthlyExpenseSummary monthlySummary = monthly.summary();
    LocalDate today = LocalDate.now(clock);
    List<ExpenseItem> items = monthly.items().stream()
        .map(item -> toExpenseItem(item, year, month))
        .toList();

    Summary summary = new Summary(
        monthlySummary.total(),
        monthlySummary.paidTotal(),
        monthlySummary.pendingTotal(),
        monthlySummary.paidCount() + monthlySummary.pendingCount(),
        monthlySummary.paidCount(),
        monthlySummary.pendingCount(),
        percentage(monthlySummary.paidTotal(), monthlySummary.total()));

    List<ExpenseItem> overdueItems = items.stream()
        .filter(item -> item.status() == PaymentStatus.PENDING)
        .filter(item -> item.dueDate().isBefore(today))
        .toList();
    Overdue overdue = new Overdue(
        overdueItems.size(),
        overdueItems.stream()
            .map(ExpenseItem::amount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));

    List<ExpenseItem> upcoming = items.stream()
        .filter(item -> item.status() == PaymentStatus.PENDING)
        .filter(item -> !item.dueDate().isBefore(today))
        .sorted(Comparator.comparing(ExpenseItem::dueDate).thenComparing(ExpenseItem::name))
        .limit(LIST_LIMIT)
        .toList();

    List<ExpenseItem> largest = items.stream()
        .sorted(Comparator.comparing(ExpenseItem::amount).reversed()
            .thenComparing(ExpenseItem::name))
        .limit(LIST_LIMIT)
        .toList();

    return new DashboardMonthlyView(
        new Competence(year, month),
        new AccountsSummary(
            accountsOverview.totalAvailableBalance(),
            accountsOverview.activeCount(),
            accountsOverview.accounts().stream()
                .map(account -> new AccountItem(
                    account.id(),
                    account.name(),
                    account.type(),
                    account.currentBalance()))
                .toList()),
        summary,
        overdue,
        upcoming,
        categoryTotals(monthly.items(), monthlySummary.total()),
        largest);
  }

  private ExpenseItem toExpenseItem(MonthlyExpenseView item, int year, int month) {
    YearMonth competence = YearMonth.of(year, month);
    int validDay = Math.min(item.dueDay(), competence.lengthOfMonth());
    return new ExpenseItem(
        item.occurrenceId(),
        item.name(),
        item.amount(),
        item.dueDay(),
        competence.atDay(validDay),
        item.categoryId(),
        item.category(),
        item.status());
  }

  private List<CategoryTotal> categoryTotals(
      List<MonthlyExpenseView> items,
      BigDecimal totalAmount) {
    Map<CategoryKey, BigDecimal> totals = new HashMap<>();
    items.forEach(item -> totals.merge(
        new CategoryKey(item.categoryId(), item.category()),
        item.amount(),
        BigDecimal::add));
    return totals.entrySet().stream()
        .map(entry -> new CategoryTotal(
            entry.getKey().id(),
            entry.getKey().name(),
            entry.getValue(),
            percentage(entry.getValue(), totalAmount)))
        .sorted(Comparator.comparing(CategoryTotal::totalAmount).reversed()
            .thenComparing(CategoryTotal::categoryName))
        .toList();
  }

  private BigDecimal percentage(BigDecimal part, BigDecimal total) {
    if (total.signum() == 0) {
      return null;
    }
    return part.multiply(ONE_HUNDRED).divide(total, 2, RoundingMode.HALF_UP);
  }

  private record CategoryKey(UUID id, String name) {
  }
}

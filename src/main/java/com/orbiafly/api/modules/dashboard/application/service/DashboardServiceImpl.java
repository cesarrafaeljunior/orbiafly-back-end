package com.orbiafly.api.modules.dashboard.application.service;

import com.orbiafly.api.modules.dashboard.application.contract.DashboardServiceInterface;
import com.orbiafly.api.modules.dashboard.application.model.DashboardMonthlyResult;
import com.orbiafly.api.modules.dashboard.application.model.DashboardMonthlyResult.AccountItem;
import com.orbiafly.api.modules.dashboard.application.model.DashboardMonthlyResult.AccountsSummary;
import com.orbiafly.api.modules.dashboard.application.model.DashboardMonthlyResult.CategoryTotal;
import com.orbiafly.api.modules.dashboard.application.model.DashboardMonthlyResult.Competence;
import com.orbiafly.api.modules.dashboard.application.model.DashboardMonthlyResult.ExpenseItem;
import com.orbiafly.api.modules.dashboard.application.model.DashboardMonthlyResult.Overdue;
import com.orbiafly.api.modules.dashboard.application.model.DashboardMonthlyResult.Summary;
import com.orbiafly.api.modules.dashboard.web.dto.response.DashboardMonthlyResponseDTO;
import com.orbiafly.api.modules.dashboard.web.factory.DashboardMonthlyResponseDTOFactory;
import com.orbiafly.api.modules.financialaccount.application.contract.FinancialAccountServiceInterface;
import com.orbiafly.api.modules.financialaccount.web.dto.response.FinancialAccountsOverviewResponseDTO;
import com.orbiafly.api.modules.fixedexpense.application.contract.FixedExpenseServiceInterface;
import com.orbiafly.api.modules.financialprojection.application.contract.MonthlyFinancialProjectionServiceInterface;
import com.orbiafly.api.modules.fixedexpense.web.dto.response.MonthlyExpenseResponseDTO;
import com.orbiafly.api.modules.fixedexpense.web.dto.response.MonthlyExpenseSummaryResponseDTO;
import com.orbiafly.api.modules.fixedexpense.web.dto.response.MonthlyExpensesResponseDTO;
import com.orbiafly.api.modules.fixedexpense.domain.PaymentStatus;
import com.orbiafly.api.modules.oneoffexpense.application.contract.OneOffExpenseServiceInterface;
import com.orbiafly.api.modules.oneoffexpense.web.dto.response.MonthlyOneOffExpensesResponseDTO;
import com.orbiafly.api.modules.oneoffexpense.web.dto.response.OneOffExpenseResponseDTO;
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
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl implements DashboardServiceInterface {

  private static final int LIST_LIMIT = 5;
  private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

  private final FixedExpenseServiceInterface fixedExpenseService;
  private final FinancialAccountServiceInterface financialAccountService;
  private final OneOffExpenseServiceInterface oneOffExpenseService;
  private final MonthlyFinancialProjectionServiceInterface projectionService;
  private final DashboardMonthlyResponseDTOFactory dashboardResponseDTOFactory;
  private final Clock clock;

  public DashboardServiceImpl(
      FixedExpenseServiceInterface fixedExpenseService,
      FinancialAccountServiceInterface financialAccountService,
      OneOffExpenseServiceInterface oneOffExpenseService,
      MonthlyFinancialProjectionServiceInterface projectionService,
      DashboardMonthlyResponseDTOFactory dashboardResponseDTOFactory,
      Clock clock) {
    this.fixedExpenseService = fixedExpenseService;
    this.financialAccountService = financialAccountService;
    this.oneOffExpenseService = oneOffExpenseService;
    this.projectionService = projectionService;
    this.dashboardResponseDTOFactory = dashboardResponseDTOFactory;
    this.clock = clock;
  }

  @Override
  public DashboardMonthlyResponseDTO getMonthlyDashboard(int year, int month) {
    MonthlyExpensesResponseDTO monthly = fixedExpenseService.getMonthlyFixedExpenses(year, month);
    MonthlyOneOffExpensesResponseDTO oneOffMonthly = oneOffExpenseService.getMonthlyOneOffExpenses(year, month);
    FinancialAccountsOverviewResponseDTO accountsOverview = financialAccountService
        .listFinancialAccounts(false);
    MonthlyExpenseSummaryResponseDTO fixedSummary = monthly.summary();
    MonthlyExpenseSummaryResponseDTO oneOffSummary = oneOffMonthly.summary();
    LocalDate today = LocalDate.now(clock);
    List<ExpenseItem> items = Stream.concat(
        monthly.items().stream().map(item -> toExpenseItem(item, year, month)),
        oneOffMonthly.items().stream().map(this::toExpenseItem))
        .toList();

    BigDecimal total = fixedSummary.total().add(oneOffSummary.total());
    BigDecimal paid = fixedSummary.paidTotal().add(oneOffSummary.paidTotal());
    BigDecimal pending = fixedSummary.pendingTotal().add(oneOffSummary.pendingTotal());
    Summary summary = new Summary(
        total, paid, pending,
        fixedSummary.totalCount() + oneOffSummary.totalCount(),
        fixedSummary.paidCount() + oneOffSummary.paidCount(),
        fixedSummary.pendingCount() + oneOffSummary.pendingCount(),
        total.signum() == 0 ? null : percentage(paid, total));

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

    DashboardMonthlyResult dashboard = new DashboardMonthlyResult(
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
        projectionService.getMonthlyProjectionResult(year, month),
        upcoming,
        categoryTotals(items, total),
        largest);
    return dashboardResponseDTOFactory.createDashboardMonthlyResponseDTO(dashboard);
  }

  private ExpenseItem toExpenseItem(MonthlyExpenseResponseDTO item, int year, int month) {
    LocalDate dueDate = item.dueDate();
    if (dueDate == null) {
      YearMonth competence = YearMonth.of(year, month);
      int validDay = Math.min(item.dueDay(), competence.lengthOfMonth());
      dueDate = competence.atDay(validDay);
    }
    return new ExpenseItem(
        item.occurrenceId(),
        item.name(),
        item.amount(),
        item.dueDay(),
        dueDate,
        item.categoryId(),
        item.category(),
        item.status());
  }

  private ExpenseItem toExpenseItem(OneOffExpenseResponseDTO item) {
    return new ExpenseItem(
        item.id(), item.name(), item.amount(), item.expenseDate().getDayOfMonth(),
        item.expenseDate(), item.categoryId(), item.category(), item.status());
  }

  private List<CategoryTotal> categoryTotals(
      List<ExpenseItem> items,
      BigDecimal totalAmount) {
    Map<CategoryKey, BigDecimal> totals = new HashMap<>();
    items.forEach(item -> totals.merge(
        new CategoryKey(item.categoryId(), item.categoryName()),
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

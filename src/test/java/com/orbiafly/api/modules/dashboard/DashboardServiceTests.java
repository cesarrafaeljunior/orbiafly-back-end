package com.orbiafly.api.modules.dashboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.orbiafly.api.modules.dashboard.application.DashboardMonthlyView;
import com.orbiafly.api.modules.dashboard.application.DashboardService;
import com.orbiafly.api.modules.financialaccount.application.FinancialAccountService;
import com.orbiafly.api.modules.financialaccount.application.FinancialAccountsOverview;
import com.orbiafly.api.modules.fixedexpense.application.FixedExpenseService;
import com.orbiafly.api.modules.fixedexpense.application.MonthlyExpenseSummary;
import com.orbiafly.api.modules.fixedexpense.application.MonthlyExpenseView;
import com.orbiafly.api.modules.fixedexpense.application.MonthlyExpensesView;
import com.orbiafly.api.modules.fixedexpense.domain.PaymentStatus;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTests {

  private static final UUID HOUSING_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID LOAN_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
  private static final UUID HOME_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");
  private static final UUID SUBSCRIPTION_ID = UUID.fromString("00000000-0000-0000-0000-000000000006");
  private static final UUID HEALTH_ID = UUID.fromString("00000000-0000-0000-0000-000000000009");

  @Mock
  private FixedExpenseService fixedExpenseService;

  @Mock
  private FinancialAccountService financialAccountService;

  private DashboardService dashboardService;

  @BeforeEach
  void setUp() {
    Clock clock = Clock.fixed(
        Instant.parse("2026-08-20T15:00:00Z"),
        ZoneId.of("America/Sao_Paulo"));
    when(financialAccountService.list(false)).thenReturn(
        new FinancialAccountsOverview(BigDecimal.ZERO, 0, List.of()));
    dashboardService = new DashboardService(fixedExpenseService, financialAccountService, clock);
  }

  @Test
  void calculatesMonthlySummaryOverdueUpcomingCategoriesAndLargestExpenses() {
    List<MonthlyExpenseView> items = List.of(
        expense("Aluguel", "900.00", 10, HOUSING_ID, "Moradia", PaymentStatus.PAID),
        expense("Empréstimo", "1000.00", 15, LOAN_ID, "Empréstimo", PaymentStatus.PENDING),
        expense("Energia", "250.00", 20, HOME_ID, "Casa", PaymentStatus.PENDING),
        expense("Spotify", "50.00", 27, SUBSCRIPTION_ID, "Assinaturas", PaymentStatus.PENDING),
        expense("Plano de saúde", "300.00", 25, HEALTH_ID, "Saúde", PaymentStatus.PAID));
    when(fixedExpenseService.getMonthly(2026, 8)).thenReturn(new MonthlyExpensesView(
        2026,
        8,
        new MonthlyExpenseSummary(
            new BigDecimal("2500.00"),
            new BigDecimal("1200.00"),
            new BigDecimal("1300.00"),
            2,
            3),
        items));

    DashboardMonthlyView dashboard = dashboardService.getMonthly(2026, 8);

    assertThat(dashboard.competence().year()).isEqualTo(2026);
    assertThat(dashboard.competence().month()).isEqualTo(8);
    assertThat(dashboard.summary().totalAmount()).isEqualByComparingTo("2500.00");
    assertThat(dashboard.summary().paidAmount()).isEqualByComparingTo("1200.00");
    assertThat(dashboard.summary().pendingAmount()).isEqualByComparingTo("1300.00");
    assertThat(dashboard.summary().totalCount()).isEqualTo(5);
    assertThat(dashboard.summary().paidCount()).isEqualTo(2);
    assertThat(dashboard.summary().pendingCount()).isEqualTo(3);
    assertThat(dashboard.summary().paidPercentage()).isEqualByComparingTo("48.00");

    assertThat(dashboard.overdue().count()).isEqualTo(1);
    assertThat(dashboard.overdue().amount()).isEqualByComparingTo("1000.00");
    assertThat(dashboard.upcomingExpenses())
        .extracting(DashboardMonthlyView.ExpenseItem::name)
        .containsExactly("Energia", "Spotify");
    assertThat(dashboard.categories())
        .extracting(DashboardMonthlyView.CategoryTotal::categoryName)
        .containsExactly("Empréstimo", "Moradia", "Saúde", "Casa", "Assinaturas");
    assertThat(dashboard.categories().getFirst().percentage()).isEqualByComparingTo("40.00");
    assertThat(dashboard.largestExpenses())
        .extracting(DashboardMonthlyView.ExpenseItem::name)
        .containsExactly("Empréstimo", "Aluguel", "Plano de saúde", "Energia", "Spotify");
  }

  @Test
  void returnsAnEmptyDashboardWithoutCalculatingPercentage() {
    when(fixedExpenseService.getMonthly(2026, 9)).thenReturn(new MonthlyExpensesView(
        2026,
        9,
        new MonthlyExpenseSummary(
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            0,
            0),
        List.of()));

    DashboardMonthlyView dashboard = dashboardService.getMonthly(2026, 9);

    assertThat(dashboard.summary().totalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(dashboard.summary().paidPercentage()).isNull();
    assertThat(dashboard.overdue().count()).isZero();
    assertThat(dashboard.upcomingExpenses()).isEmpty();
    assertThat(dashboard.categories()).isEmpty();
    assertThat(dashboard.largestExpenses()).isEmpty();
  }

  @Test
  void adjustsDueDateToTheLastValidDayOfTheCompetence() {
    MonthlyExpenseView item = expense(
        "Vencimento no fim do mês",
        "100.00",
        31,
        HOME_ID,
        "Casa",
        PaymentStatus.PENDING);
    when(fixedExpenseService.getMonthly(2026, 2)).thenReturn(new MonthlyExpensesView(
        2026,
        2,
        new MonthlyExpenseSummary(
            new BigDecimal("100.00"),
            BigDecimal.ZERO,
            new BigDecimal("100.00"),
            0,
            1),
        List.of(item)));

    DashboardMonthlyView dashboard = dashboardService.getMonthly(2026, 2);

    assertThat(dashboard.largestExpenses().getFirst().dueDate())
        .isEqualTo(LocalDate.of(2026, 2, 28));
  }

  private MonthlyExpenseView expense(
      String name,
      String amount,
      int dueDay,
      UUID categoryId,
      String category,
      PaymentStatus status) {
    UUID id = UUID.randomUUID();
    return new MonthlyExpenseView(
        id,
        UUID.randomUUID(),
        name,
        new BigDecimal(amount),
        new BigDecimal(amount),
        dueDay,
        categoryId,
        category,
        status,
        status == PaymentStatus.PAID ? Instant.parse("2026-08-10T12:00:00Z") : null,
        null,
        null,
        true);
  }
}

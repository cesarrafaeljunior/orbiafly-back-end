package com.orbiafly.api.modules.dashboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.orbiafly.api.modules.dashboard.application.contract.DashboardServiceInterface;
import com.orbiafly.api.modules.dashboard.application.service.DashboardServiceImpl;
import com.orbiafly.api.modules.dashboard.web.dto.response.DashboardMonthlyResponseDTO;
import com.orbiafly.api.modules.dashboard.web.factory.DashboardMonthlyResponseDTOFactory;
import com.orbiafly.api.modules.financialaccount.application.contract.FinancialAccountServiceInterface;
import com.orbiafly.api.modules.financialprojection.application.contract.MonthlyFinancialProjectionServiceInterface;
import com.orbiafly.api.modules.financialprojection.application.model.MonthlyFinancialProjectionResult;
import com.orbiafly.api.modules.financialprojection.domain.FinancialHealthLevel;
import com.orbiafly.api.modules.financialprojection.web.factory.MonthlyFinancialProjectionResponseDTOFactory;
import com.orbiafly.api.modules.financialaccount.web.dto.response.FinancialAccountsOverviewResponseDTO;
import com.orbiafly.api.modules.fixedexpense.application.contract.FixedExpenseServiceInterface;
import com.orbiafly.api.modules.fixedexpense.domain.PaymentStatus;
import com.orbiafly.api.modules.fixedexpense.web.dto.response.MonthlyExpenseResponseDTO;
import com.orbiafly.api.modules.fixedexpense.web.dto.response.MonthlyExpenseSummaryResponseDTO;
import com.orbiafly.api.modules.fixedexpense.web.dto.response.MonthlyExpensesResponseDTO;
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
  private FixedExpenseServiceInterface fixedExpenseService;

  @Mock
  private FinancialAccountServiceInterface financialAccountService;

  @Mock
  private MonthlyFinancialProjectionServiceInterface projectionService;

  private DashboardServiceInterface dashboardService;

  @BeforeEach
  void setUp() {
    Clock clock = Clock.fixed(
        Instant.parse("2026-08-20T15:00:00Z"),
        ZoneId.of("America/Sao_Paulo"));
    when(financialAccountService.listFinancialAccounts(false)).thenReturn(
        new FinancialAccountsOverviewResponseDTO(BigDecimal.ZERO, 0, List.of()));
    lenient().when(projectionService.getMonthlyProjectionResult(2026, 8)).thenReturn(projection(2026, 8));
    lenient().when(projectionService.getMonthlyProjectionResult(2026, 9)).thenReturn(projection(2026, 9));
    lenient().when(projectionService.getMonthlyProjectionResult(2026, 2)).thenReturn(projection(2026, 2));
    dashboardService = new DashboardServiceImpl(
        fixedExpenseService,
        financialAccountService,
        projectionService,
        new DashboardMonthlyResponseDTOFactory(new MonthlyFinancialProjectionResponseDTOFactory()),
        clock);
  }

  @Test
  void calculatesMonthlySummaryOverdueUpcomingCategoriesAndLargestExpenses() {
    List<MonthlyExpenseResponseDTO> items = List.of(
        expense("Aluguel", "900.00", 10, HOUSING_ID, "Moradia", PaymentStatus.PAID),
        expense("Empréstimo", "1000.00", 15, LOAN_ID, "Empréstimo", PaymentStatus.PENDING),
        expense("Energia", "250.00", 20, HOME_ID, "Casa", PaymentStatus.PENDING),
        expense("Spotify", "50.00", 27, SUBSCRIPTION_ID, "Assinaturas", PaymentStatus.PENDING),
        expense("Plano de saúde", "300.00", 25, HEALTH_ID, "Saúde", PaymentStatus.PAID));
    when(fixedExpenseService.getMonthlyFixedExpenses(2026, 8)).thenReturn(new MonthlyExpensesResponseDTO(
        2026,
        8,
        new MonthlyExpenseSummaryResponseDTO(
            new BigDecimal("2500.00"),
            new BigDecimal("1200.00"),
            new BigDecimal("1300.00"),
            5,
            2,
            3,
            new BigDecimal("48.00")),
        items));

    DashboardMonthlyResponseDTO dashboard = dashboardService.getMonthlyDashboard(2026, 8);

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
        .extracting(DashboardMonthlyResponseDTO.ExpenseItem::name)
        .containsExactly("Energia", "Spotify");
    assertThat(dashboard.categories())
        .extracting(DashboardMonthlyResponseDTO.CategoryTotal::categoryName)
        .containsExactly("Empréstimo", "Moradia", "Saúde", "Casa", "Assinaturas");
    assertThat(dashboard.categories().getFirst().percentage()).isEqualByComparingTo("40.00");
    assertThat(dashboard.largestExpenses())
        .extracting(DashboardMonthlyResponseDTO.ExpenseItem::name)
        .containsExactly("Empréstimo", "Aluguel", "Plano de saúde", "Energia", "Spotify");
  }

  @Test
  void returnsAnEmptyDashboardWithoutCalculatingPercentage() {
    when(fixedExpenseService.getMonthlyFixedExpenses(2026, 9)).thenReturn(new MonthlyExpensesResponseDTO(
        2026,
        9,
        new MonthlyExpenseSummaryResponseDTO(
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            0,
            0,
            0,
            BigDecimal.ZERO),
        List.of()));

    DashboardMonthlyResponseDTO dashboard = dashboardService.getMonthlyDashboard(2026, 9);

    assertThat(dashboard.summary().totalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(dashboard.summary().paidPercentage()).isNull();
    assertThat(dashboard.overdue().count()).isZero();
    assertThat(dashboard.upcomingExpenses()).isEmpty();
    assertThat(dashboard.categories()).isEmpty();
    assertThat(dashboard.largestExpenses()).isEmpty();
  }

  @Test
  void adjustsDueDateToTheLastValidDayOfTheCompetence() {
    MonthlyExpenseResponseDTO item = expense(
        "Vencimento no fim do mês",
        "100.00",
        31,
        HOME_ID,
        "Casa",
        PaymentStatus.PENDING);
    when(fixedExpenseService.getMonthlyFixedExpenses(2026, 2)).thenReturn(new MonthlyExpensesResponseDTO(
        2026,
        2,
        new MonthlyExpenseSummaryResponseDTO(
            new BigDecimal("100.00"),
            BigDecimal.ZERO,
            new BigDecimal("100.00"),
            1,
            0,
            1,
            BigDecimal.ZERO),
        List.of(item)));

    DashboardMonthlyResponseDTO dashboard = dashboardService.getMonthlyDashboard(2026, 2);

    assertThat(dashboard.largestExpenses().getFirst().dueDate())
        .isEqualTo(LocalDate.of(2026, 2, 28));
  }

  private MonthlyFinancialProjectionResult projection(int year, int month) {
    return new MonthlyFinancialProjectionResult(
        year,
        month,
        BigDecimal.ZERO,
        BigDecimal.ZERO,
        BigDecimal.ZERO,
        BigDecimal.ZERO,
        BigDecimal.ZERO,
        LocalDate.of(year, month, 1),
        FinancialHealthLevel.HEALTHY,
        "Sem eventos projetados.",
        List.of());
  }

  private MonthlyExpenseResponseDTO expense(
      String name,
      String amount,
      int dueDay,
      UUID categoryId,
      String category,
      PaymentStatus status) {
    UUID id = UUID.randomUUID();
    return new MonthlyExpenseResponseDTO(
        id,
        UUID.randomUUID(),
        name,
        new BigDecimal(amount),
        new BigDecimal(amount),
        null,
        dueDay,
        categoryId,
        category,
        status,
        status == PaymentStatus.PAID ? Instant.parse("2026-08-10T12:00:00Z") : null,
        null,
        null,
        false,
        null,
        null,
        true);
  }
}

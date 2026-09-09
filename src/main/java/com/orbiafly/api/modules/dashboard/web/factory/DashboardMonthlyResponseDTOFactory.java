package com.orbiafly.api.modules.dashboard.web.factory;

import com.orbiafly.api.modules.dashboard.application.model.DashboardMonthlyResult;
import com.orbiafly.api.modules.dashboard.web.dto.response.DashboardMonthlyResponseDTO;
import com.orbiafly.api.modules.financialprojection.web.factory.MonthlyFinancialProjectionResponseDTOFactory;
import org.springframework.stereotype.Component;

@Component
public class DashboardMonthlyResponseDTOFactory {

  private final MonthlyFinancialProjectionResponseDTOFactory projectionFactory;

  public DashboardMonthlyResponseDTOFactory(
      MonthlyFinancialProjectionResponseDTOFactory projectionFactory) {
    this.projectionFactory = projectionFactory;
  }

  public DashboardMonthlyResponseDTO createDashboardMonthlyResponseDTO(
      DashboardMonthlyResult dashboard) {
    return new DashboardMonthlyResponseDTO(
        competenceFrom(dashboard.competence()),
        accountsFrom(dashboard.accounts()),
        summaryFrom(dashboard.summary()),
        overdueFrom(dashboard.overdue()),
        projectionFactory.createMonthlyFinancialProjectionResponseDTO(dashboard.projection()),
        dashboard.upcomingExpenses().stream()
            .map(this::expenseFrom)
            .toList(),
        dashboard.categories().stream()
            .map(this::categoryFrom)
            .toList(),
        dashboard.largestExpenses().stream()
            .map(this::expenseFrom)
            .toList());
  }

  private DashboardMonthlyResponseDTO.Competence competenceFrom(
      DashboardMonthlyResult.Competence competence) {
    return new DashboardMonthlyResponseDTO.Competence(
        competence.year(),
        competence.month());
  }

  private DashboardMonthlyResponseDTO.AccountsSummary accountsFrom(
      DashboardMonthlyResult.AccountsSummary accounts) {
    return new DashboardMonthlyResponseDTO.AccountsSummary(
        accounts.availableBalance(),
        accounts.activeCount(),
        accounts.items().stream()
            .map(this::accountFrom)
            .toList());
  }

  private DashboardMonthlyResponseDTO.AccountItem accountFrom(
      DashboardMonthlyResult.AccountItem account) {
    return new DashboardMonthlyResponseDTO.AccountItem(
        account.id(),
        account.name(),
        account.type(),
        account.currentBalance());
  }

  private DashboardMonthlyResponseDTO.Summary summaryFrom(
      DashboardMonthlyResult.Summary summary) {
    return new DashboardMonthlyResponseDTO.Summary(
        summary.totalAmount(),
        summary.paidAmount(),
        summary.pendingAmount(),
        summary.totalCount(),
        summary.paidCount(),
        summary.pendingCount(),
        summary.paidPercentage());
  }

  private DashboardMonthlyResponseDTO.Overdue overdueFrom(
      DashboardMonthlyResult.Overdue overdue) {
    return new DashboardMonthlyResponseDTO.Overdue(
        overdue.count(),
        overdue.amount());
  }

  private DashboardMonthlyResponseDTO.ExpenseItem expenseFrom(
      DashboardMonthlyResult.ExpenseItem expense) {
    return new DashboardMonthlyResponseDTO.ExpenseItem(
        expense.occurrenceId(),
        expense.name(),
        expense.amount(),
        expense.dueDay(),
        expense.dueDate(),
        expense.categoryId(),
        expense.categoryName(),
        expense.status());
  }

  private DashboardMonthlyResponseDTO.CategoryTotal categoryFrom(
      DashboardMonthlyResult.CategoryTotal category) {
    return new DashboardMonthlyResponseDTO.CategoryTotal(
        category.categoryId(),
        category.categoryName(),
        category.totalAmount(),
        category.percentage());
  }
}

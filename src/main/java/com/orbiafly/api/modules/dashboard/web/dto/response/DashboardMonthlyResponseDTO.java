package com.orbiafly.api.modules.dashboard.web.dto.response;

import com.orbiafly.api.modules.financialaccount.domain.FinancialAccountType;
import com.orbiafly.api.modules.fixedexpense.domain.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record DashboardMonthlyResponseDTO(
    Competence competence,
    AccountsSummary accounts,
    Summary summary,
    Overdue overdue,
    List<ExpenseItem> upcomingExpenses,
    List<CategoryTotal> categories,
    List<ExpenseItem> largestExpenses) {

  public record Competence(int year, int month) {
  }

  public record AccountsSummary(
      BigDecimal availableBalance,
      int activeCount,
      List<AccountItem> items) {
  }

  public record AccountItem(
      UUID id,
      String name,
      FinancialAccountType type,
      BigDecimal currentBalance) {
  }

  public record Summary(
      BigDecimal totalAmount,
      BigDecimal paidAmount,
      BigDecimal pendingAmount,
      int totalCount,
      int paidCount,
      int pendingCount,
      BigDecimal paidPercentage) {
  }

  public record Overdue(int count, BigDecimal amount) {
  }

  public record ExpenseItem(
      UUID occurrenceId,
      String name,
      BigDecimal amount,
      int dueDay,
      LocalDate dueDate,
      UUID categoryId,
      String categoryName,
      PaymentStatus status) {
  }

  public record CategoryTotal(
      UUID categoryId,
      String categoryName,
      BigDecimal totalAmount,
      BigDecimal percentage) {
  }
}

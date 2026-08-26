package com.orbiafly.api.modules.fixedexpense.application.contract;

import com.orbiafly.api.modules.fixedexpense.domain.PaymentStatus;
import com.orbiafly.api.modules.fixedexpense.web.dto.response.CategoryResponseDTO;
import com.orbiafly.api.modules.fixedexpense.web.dto.response.FixedExpensePaymentResponseDTO;
import com.orbiafly.api.modules.fixedexpense.web.dto.response.FixedExpenseResponseDTO;
import com.orbiafly.api.modules.fixedexpense.web.dto.response.MonthlyExpenseResponseDTO;
import com.orbiafly.api.modules.fixedexpense.web.dto.response.MonthlyExpensesResponseDTO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface FixedExpenseServiceInterface {

  List<CategoryResponseDTO> listFixedExpenseCategories();

  List<FixedExpenseResponseDTO> listFixedExpenses();

  FixedExpenseResponseDTO createFixedExpense(
      String name,
      BigDecimal defaultAmount,
      int dueDay,
      UUID categoryId);

  FixedExpenseResponseDTO updateFixedExpense(
      UUID id,
      String name,
      BigDecimal defaultAmount,
      int dueDay,
      UUID categoryId,
      boolean active);

  MonthlyExpensesResponseDTO getMonthlyFixedExpenses(int year, int month);

  MonthlyExpenseResponseDTO updateFixedExpenseOccurrenceAmount(
      UUID occurrenceId,
      BigDecimal amount);

  MonthlyExpenseResponseDTO updateFixedExpenseOccurrenceStatus(
      UUID occurrenceId,
      PaymentStatus status);

  FixedExpensePaymentResponseDTO registerFixedExpenseOccurrencePayment(
      UUID occurrenceId,
      UUID financialAccountId,
      LocalDate paymentDate);
}

package com.orbiafly.api.modules.oneoffexpense.application.contract;

import com.orbiafly.api.modules.fixedexpense.domain.PaymentStatus;
import com.orbiafly.api.modules.oneoffexpense.web.dto.response.MonthlyOneOffExpensesResponseDTO;
import com.orbiafly.api.modules.oneoffexpense.web.dto.response.OneOffExpensePaymentResponseDTO;
import com.orbiafly.api.modules.oneoffexpense.web.dto.response.OneOffExpenseResponseDTO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface OneOffExpenseServiceInterface {
  MonthlyOneOffExpensesResponseDTO getMonthlyOneOffExpenses(int year, int month);
  OneOffExpenseResponseDTO createOneOffExpense(String name, BigDecimal amount, LocalDate expenseDate, UUID categoryId);
  OneOffExpenseResponseDTO updateOneOffExpense(UUID id, String name, BigDecimal amount, LocalDate expenseDate, UUID categoryId);
  OneOffExpenseResponseDTO updateOneOffExpenseStatus(UUID id, PaymentStatus status);
  OneOffExpensePaymentResponseDTO registerOneOffExpensePayment(UUID id, UUID financialAccountId, LocalDate paymentDate);
  void deleteOneOffExpense(UUID id);
}

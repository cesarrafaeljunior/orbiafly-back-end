package com.orbiafly.api.modules.fixedexpense.web.factory;

import com.orbiafly.api.modules.financialaccount.web.factory.FinancialAccountResponseDTOFactory;
import com.orbiafly.api.modules.financialaccount.web.factory.FinancialTransactionResponseDTOFactory;
import com.orbiafly.api.modules.fixedexpense.application.model.FixedExpensePaymentResult;
import com.orbiafly.api.modules.fixedexpense.web.dto.response.FixedExpensePaymentResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class FixedExpensePaymentResponseDTOFactory {

  private final MonthlyExpenseResponseDTOFactory monthlyExpenseFactory;
  private final FinancialTransactionResponseDTOFactory transactionFactory;
  private final FinancialAccountResponseDTOFactory accountFactory;

  public FixedExpensePaymentResponseDTOFactory(
      MonthlyExpenseResponseDTOFactory monthlyExpenseFactory,
      FinancialTransactionResponseDTOFactory transactionFactory,
      FinancialAccountResponseDTOFactory accountFactory) {
    this.monthlyExpenseFactory = monthlyExpenseFactory;
    this.transactionFactory = transactionFactory;
    this.accountFactory = accountFactory;
  }

  public FixedExpensePaymentResponseDTO createFixedExpensePaymentResponseDTO(
      FixedExpensePaymentResult payment) {
    return new FixedExpensePaymentResponseDTO(
        monthlyExpenseFactory.createMonthlyExpenseResponseDTO(payment.occurrence()),
        transactionFactory.createFinancialTransactionResponseDTO(payment.payment()),
        accountFactory.createFinancialAccountResponseDTO(payment.financialAccount()),
        payment.updatedBalance(),
        payment.negativeBalance());
  }
}

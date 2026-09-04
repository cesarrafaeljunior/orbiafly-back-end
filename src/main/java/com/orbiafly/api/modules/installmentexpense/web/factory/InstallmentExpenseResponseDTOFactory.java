package com.orbiafly.api.modules.installmentexpense.web.factory;

import com.orbiafly.api.modules.financialaccount.web.factory.FinancialAccountResponseDTOFactory;
import com.orbiafly.api.modules.financialaccount.web.factory.FinancialTransactionResponseDTOFactory;
import com.orbiafly.api.modules.installmentexpense.application.model.InstallmentPaymentResult;
import com.orbiafly.api.modules.installmentexpense.application.model.InstallmentPurchaseResult;
import com.orbiafly.api.modules.installmentexpense.application.model.InstallmentResult;
import com.orbiafly.api.modules.installmentexpense.web.dto.response.InstallmentPaymentResponseDTO;
import com.orbiafly.api.modules.installmentexpense.web.dto.response.InstallmentPurchaseResponseDTO;
import com.orbiafly.api.modules.installmentexpense.web.dto.response.InstallmentResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class InstallmentExpenseResponseDTOFactory {

  private final FinancialAccountResponseDTOFactory accountResponseDTOFactory;
  private final FinancialTransactionResponseDTOFactory transactionResponseDTOFactory;

  public InstallmentExpenseResponseDTOFactory(
      FinancialAccountResponseDTOFactory accountResponseDTOFactory,
      FinancialTransactionResponseDTOFactory transactionResponseDTOFactory) {
    this.accountResponseDTOFactory = accountResponseDTOFactory;
    this.transactionResponseDTOFactory = transactionResponseDTOFactory;
  }

  public InstallmentPurchaseResponseDTO createInstallmentPurchaseResponseDTO(
      InstallmentPurchaseResult result) {
    return new InstallmentPurchaseResponseDTO(
        result.id(),
        result.description(),
        result.totalAmount(),
        result.installmentCount(),
        result.firstInstallmentDate(),
        result.paidAmount(),
        result.pendingAmount(),
        result.installments().stream().map(this::createInstallmentResponseDTO).toList(),
        result.createdAt(),
        result.updatedAt());
  }

  public InstallmentResponseDTO createInstallmentResponseDTO(InstallmentResult result) {
    return new InstallmentResponseDTO(
        result.id(),
        result.installmentNumber(),
        result.amount(),
        result.dueDate(),
        result.status(),
        result.paidAt(),
        result.financialAccountId(),
        result.paymentTransactionId(),
        result.createdAt(),
        result.updatedAt());
  }

  public InstallmentPaymentResponseDTO createInstallmentPaymentResponseDTO(InstallmentPaymentResult result) {
    return new InstallmentPaymentResponseDTO(
        createInstallmentResponseDTO(result.installment()),
        transactionResponseDTOFactory.createFinancialTransactionResponseDTO(result.transaction()),
        accountResponseDTOFactory.createFinancialAccountResponseDTO(result.financialAccount()));
  }
}

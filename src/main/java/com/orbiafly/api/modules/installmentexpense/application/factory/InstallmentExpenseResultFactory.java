package com.orbiafly.api.modules.installmentexpense.application.factory;

import com.orbiafly.api.modules.fixedexpense.domain.PaymentStatus;
import com.orbiafly.api.modules.installmentexpense.application.model.InstallmentPurchaseResult;
import com.orbiafly.api.modules.installmentexpense.application.model.InstallmentResult;
import com.orbiafly.api.modules.installmentexpense.domain.InstallmentExpenseInstallment;
import com.orbiafly.api.modules.installmentexpense.domain.InstallmentPurchase;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class InstallmentExpenseResultFactory {

  public InstallmentPurchaseResult createInstallmentPurchaseResult(InstallmentPurchase purchase) {
    var installments = purchase.getInstallments().stream()
        .map(this::createInstallmentResult)
        .toList();
    BigDecimal paidAmount = installments.stream()
        .filter(installment -> installment.status() == PaymentStatus.PAID)
        .map(InstallmentResult::amount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    return new InstallmentPurchaseResult(
        purchase.getId(),
        purchase.getDescription(),
        purchase.getTotalAmount(),
        purchase.getInstallmentCount(),
        purchase.getFirstInstallmentDate(),
        paidAmount,
        purchase.getTotalAmount().subtract(paidAmount),
        installments,
        purchase.getCreatedAt(),
        purchase.getUpdatedAt());
  }

  public InstallmentResult createInstallmentResult(InstallmentExpenseInstallment installment) {
    return new InstallmentResult(
        installment.getId(),
        installment.getInstallmentNumber(),
        installment.getAmount(),
        installment.getDueDate(),
        installment.getStatus(),
        installment.getPaidAt(),
        installment.getFinancialAccountId(),
        installment.getPaymentTransactionId(),
        installment.getCreatedAt(),
        installment.getUpdatedAt());
  }
}

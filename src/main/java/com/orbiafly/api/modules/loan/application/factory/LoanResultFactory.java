package com.orbiafly.api.modules.loan.application.factory;

import com.orbiafly.api.modules.loan.application.model.LoanEntryResult;
import com.orbiafly.api.modules.loan.application.model.LoanInstallmentResult;
import com.orbiafly.api.modules.loan.application.model.LoanResult;
import com.orbiafly.api.modules.loan.domain.Loan;
import com.orbiafly.api.modules.loan.domain.LoanDirection;
import com.orbiafly.api.modules.loan.domain.LoanInstallment;
import com.orbiafly.api.modules.loan.domain.LoanInstallmentStatus;
import com.orbiafly.api.modules.loan.domain.LoanStatus;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class LoanResultFactory {

  private final LoanInstallmentResultFactory installmentResultFactory;
  private final LoanEntryResultFactory entryResultFactory;

  public LoanResultFactory(
      LoanInstallmentResultFactory installmentResultFactory,
      LoanEntryResultFactory entryResultFactory) {
    this.installmentResultFactory = installmentResultFactory;
    this.entryResultFactory = entryResultFactory;
  }

  public LoanResult createLoanResult(Loan loan, Map<UUID, String> financialAccountNames) {
    List<LoanInstallment> installments = loan.getInstallments().stream()
        .sorted(Comparator.comparingInt(LoanInstallment::getInstallmentNumber))
        .toList();
    List<LoanInstallmentResult> installmentResults = installments.stream()
        .map(installmentResultFactory::createLoanInstallmentResult)
        .toList();
    LoanInstallmentResult nextInstallment = installments.stream()
        .filter(installment -> installment.getStatus() == LoanInstallmentStatus.PENDING)
        .findFirst()
        .map(installmentResultFactory::createLoanInstallmentResult)
        .orElse(null);
    int completedInstallmentCount = (int) installments.stream()
        .filter(installment -> installment.getStatus() == LoanInstallmentStatus.COMPLETED)
        .count();
    List<LoanEntryResult> entries = loan.getEntries().stream()
        .sorted(Comparator
            .comparing(com.orbiafly.api.modules.loan.domain.LoanEntry::getOccurredOn)
            .reversed()
            .thenComparing(com.orbiafly.api.modules.loan.domain.LoanEntry::getCreatedAt)
            .reversed())
        .map(entry -> entryResultFactory.createLoanEntryResult(entry, financialAccountNames))
        .toList();
    BigDecimal settledAmount = loan.totalSettled();
    BigDecimal remainingAmount = loan.remainingAmount();
    BigDecimal progressPercentage = loan.getTotalAmount().signum() == 0
        ? BigDecimal.ZERO
        : settledAmount
            .min(loan.getTotalAmount())
            .multiply(BigDecimal.valueOf(100))
            .divide(loan.getTotalAmount(), 2, RoundingMode.HALF_UP);
    LocalDate estimatedEndDate = installments.isEmpty()
        ? null
        : installments.get(installments.size() - 1).getDueDate();

    return new LoanResult(
        loan.getId(),
        loan.getName(),
        loan.getDirection(),
        loan.getCounterparty(),
        loan.getTotalAmount(),
        loan.getStartDate(),
        loan.getInstallmentCount(),
        loan.getInstallmentAmount(),
        loan.getFirstDueDate(),
        loan.getInterestRate(),
        loan.getNotes(),
        remainingAmount.signum() == 0 ? LoanStatus.COMPLETED : LoanStatus.ACTIVE,
        loan.getDirection() == LoanDirection.BORROWED ? settledAmount : settledAmount,
        remainingAmount,
        progressPercentage,
        completedInstallmentCount,
        nextInstallment,
        estimatedEndDate,
        installmentResults,
        entries,
        loan.getDeletedAt(),
        loan.getCreatedAt(),
        loan.getUpdatedAt());
  }
}

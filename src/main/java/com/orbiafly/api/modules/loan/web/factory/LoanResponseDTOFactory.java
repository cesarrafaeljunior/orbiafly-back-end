package com.orbiafly.api.modules.loan.web.factory;

import com.orbiafly.api.modules.loan.application.model.LoanResult;
import com.orbiafly.api.modules.loan.web.dto.response.LoanResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class LoanResponseDTOFactory {

  private final LoanInstallmentResponseDTOFactory installmentFactory;
  private final LoanEntryResponseDTOFactory entryFactory;

  public LoanResponseDTOFactory(
      LoanInstallmentResponseDTOFactory installmentFactory,
      LoanEntryResponseDTOFactory entryFactory) {
    this.installmentFactory = installmentFactory;
    this.entryFactory = entryFactory;
  }

  public LoanResponseDTO createLoanResponseDTO(LoanResult loan) {
    return new LoanResponseDTO(
        loan.id(),
        loan.name(),
        loan.direction(),
        loan.counterparty(),
        loan.totalAmount(),
        loan.startDate(),
        loan.installmentCount(),
        loan.installmentAmount(),
        loan.firstDueDate(),
        loan.interestRate(),
        loan.notes(),
        loan.status(),
        loan.settledAmount(),
        loan.remainingAmount(),
        loan.progressPercentage(),
        loan.completedInstallmentCount(),
        installmentFactory.createLoanInstallmentResponseDTO(loan.nextInstallment()),
        loan.estimatedEndDate(),
        loan.installments().stream()
            .map(installmentFactory::createLoanInstallmentResponseDTO)
            .toList(),
        loan.entries().stream()
            .map(entryFactory::createLoanEntryResponseDTO)
            .toList(),
        loan.deletedAt(),
        loan.createdAt(),
        loan.updatedAt());
  }
}

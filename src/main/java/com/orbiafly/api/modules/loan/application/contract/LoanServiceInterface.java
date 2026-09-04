package com.orbiafly.api.modules.loan.application.contract;

import com.orbiafly.api.modules.loan.domain.LoanDirection;
import com.orbiafly.api.modules.loan.web.dto.response.LoanOverviewResponseDTO;
import com.orbiafly.api.modules.loan.web.dto.response.LoanResponseDTO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface LoanServiceInterface {

  LoanOverviewResponseDTO listLoans();

  LoanResponseDTO getLoan(UUID id);

  LoanResponseDTO createLoan(
      String name,
      LoanDirection direction,
      String counterparty,
      BigDecimal totalAmount,
      LocalDate startDate,
      Integer installmentCount,
      BigDecimal installmentAmount,
      LocalDate firstDueDate,
      String interestRate,
      String notes);

  LoanResponseDTO registerLoanEntry(
      UUID id,
      UUID financialAccountId,
      BigDecimal amount,
      LocalDate occurredOn);

  void deleteLoan(UUID id);
}

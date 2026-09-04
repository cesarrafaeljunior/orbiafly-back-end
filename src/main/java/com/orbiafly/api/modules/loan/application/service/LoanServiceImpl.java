package com.orbiafly.api.modules.loan.application.service;

import com.orbiafly.api.infrastructure.security.CurrentUserProvider;
import com.orbiafly.api.modules.financialaccount.application.contract.FinancialAccountServiceInterface;
import com.orbiafly.api.modules.financialaccount.application.model.AccountTransactionResult;
import com.orbiafly.api.modules.financialaccount.domain.FinancialReferenceType;
import com.orbiafly.api.modules.fixedexpense.application.ResourceNotFoundException;
import com.orbiafly.api.modules.loan.application.contract.LoanServiceInterface;
import com.orbiafly.api.modules.loan.application.factory.LoanResultFactory;
import com.orbiafly.api.modules.loan.application.model.LoanOverviewResult;
import com.orbiafly.api.modules.loan.application.model.LoanResult;
import com.orbiafly.api.modules.loan.domain.Loan;
import com.orbiafly.api.modules.loan.domain.LoanDirection;
import com.orbiafly.api.modules.loan.domain.LoanEntry;
import com.orbiafly.api.modules.loan.domain.LoanInstallment;
import com.orbiafly.api.modules.loan.domain.LoanStatus;
import com.orbiafly.api.modules.loan.infrastructure.persistence.LoanRepositoryInterface;
import com.orbiafly.api.modules.loan.web.dto.response.LoanOverviewResponseDTO;
import com.orbiafly.api.modules.loan.web.dto.response.LoanResponseDTO;
import com.orbiafly.api.modules.loan.web.factory.LoanOverviewResponseDTOFactory;
import com.orbiafly.api.modules.loan.web.factory.LoanResponseDTOFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanServiceImpl implements LoanServiceInterface {

  private final LoanRepositoryInterface loanRepository;
  private final FinancialAccountServiceInterface financialAccountService;
  private final LoanResultFactory loanResultFactory;
  private final LoanResponseDTOFactory loanResponseDTOFactory;
  private final LoanOverviewResponseDTOFactory overviewResponseDTOFactory;
  private final CurrentUserProvider currentUserProvider;
  private final Clock clock;

  public LoanServiceImpl(
      LoanRepositoryInterface loanRepository,
      FinancialAccountServiceInterface financialAccountService,
      LoanResultFactory loanResultFactory,
      LoanResponseDTOFactory loanResponseDTOFactory,
      LoanOverviewResponseDTOFactory overviewResponseDTOFactory,
      CurrentUserProvider currentUserProvider,
      Clock clock) {
    this.loanRepository = loanRepository;
    this.financialAccountService = financialAccountService;
    this.loanResultFactory = loanResultFactory;
    this.loanResponseDTOFactory = loanResponseDTOFactory;
    this.overviewResponseDTOFactory = overviewResponseDTOFactory;
    this.currentUserProvider = currentUserProvider;
    this.clock = clock;
  }

  @Override
  @Transactional(readOnly = true)
  public LoanOverviewResponseDTO listLoans() {
    List<Loan> loans = loanRepository
        .findAllByOwnerUserIdAndDeletedAtIsNullOrderByStatusAscStartDateDescCreatedAtDesc(
            currentUserProvider.currentUserId());
    Map<UUID, String> accountNames = accountNames(loans);
    List<LoanResult> loanResults = loans.stream()
        .map(loan -> loanResultFactory.createLoanResult(loan, accountNames))
        .toList();
    BigDecimal totalBorrowedRemaining = loanResults.stream()
        .filter(loan -> loan.direction() == LoanDirection.BORROWED)
        .filter(loan -> loan.status() == LoanStatus.ACTIVE)
        .map(LoanResult::remainingAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal totalLentRemaining = loanResults.stream()
        .filter(loan -> loan.direction() == LoanDirection.LENT)
        .filter(loan -> loan.status() == LoanStatus.ACTIVE)
        .map(LoanResult::remainingAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    int activeLoanCount = (int) loanResults.stream()
        .filter(loan -> loan.status() == LoanStatus.ACTIVE)
        .count();
    int upcomingEntryCount = (int) loanResults.stream()
        .filter(loan -> loan.status() == LoanStatus.ACTIVE)
        .filter(loan -> loan.nextInstallment() != null)
        .count();

    return overviewResponseDTOFactory.createLoanOverviewResponseDTO(
        new LoanOverviewResult(
            totalBorrowedRemaining,
            totalLentRemaining,
            activeLoanCount,
            upcomingEntryCount,
            loanResults));
  }

  @Override
  @Transactional(readOnly = true)
  public LoanResponseDTO getLoan(UUID id) {
    Loan loan = loanRepository.findByIdAndOwnerUserIdAndDeletedAtIsNull(
            id,
            currentUserProvider.currentUserId())
        .orElseThrow(() -> new ResourceNotFoundException("Empréstimo não encontrado."));
    return loanResponseDTOFactory.createLoanResponseDTO(
        loanResultFactory.createLoanResult(loan, accountNames(List.of(loan))));
  }

  @Override
  @Transactional
  public LoanResponseDTO createLoan(
      String name,
      LoanDirection direction,
      String counterparty,
      BigDecimal totalAmount,
      LocalDate startDate,
      Integer installmentCount,
      BigDecimal installmentAmount,
      LocalDate firstDueDate,
      String interestRate,
      String notes) {
    validateLoan(totalAmount, installmentCount, installmentAmount, firstDueDate);
    Instant now = clock.instant();
    Loan loan = new Loan(
        normalizeRequired(name),
        direction,
        normalizeRequired(counterparty),
        totalAmount,
        startDate,
        installmentCount,
        installmentAmount,
        firstDueDate,
        normalizeOptional(interestRate),
        normalizeOptional(notes),
        currentUserProvider.currentUserId(),
        now);
    if (installmentCount != null) {
      createInstallments(loan, totalAmount, installmentAmount, installmentCount, firstDueDate, now);
    }
    Loan saved = loanRepository.save(loan);
    return loanResponseDTOFactory.createLoanResponseDTO(
        loanResultFactory.createLoanResult(saved, Map.of()));
  }

  @Override
  @Transactional
  public LoanResponseDTO registerLoanEntry(
      UUID id,
      UUID financialAccountId,
      BigDecimal amount,
      LocalDate occurredOn) {
    Loan loan = loanRepository.findByIdAndOwnerUserIdForUpdate(
            id,
            currentUserProvider.currentUserId())
        .orElseThrow(() -> new ResourceNotFoundException("Empréstimo não encontrado."));
    if (loan.getStatus() == LoanStatus.COMPLETED || loan.remainingAmount().signum() == 0) {
      throw new IllegalStateException("Este empréstimo já está quitado.");
    }
    BigDecimal entryAmount = entryAmount(loan, amount);
    if (entryAmount.compareTo(loan.remainingAmount()) > 0) {
      throw new IllegalArgumentException("O valor informado excede o saldo restante.");
    }
    LoanInstallment installment = loan.nextPendingInstallment();
    Instant now = clock.instant();
    LoanEntry entry = new LoanEntry(
        loan,
        installment,
        entryAmount,
        occurredOn,
        financialAccountId,
        null,
        loan.getOwnerUserId(),
        now);

    AccountTransactionResult transaction = loan.getDirection() == LoanDirection.BORROWED
        ? financialAccountService.debitFinancialAccountForReference(
            financialAccountId,
            entryAmount,
            "Pagamento de empréstimo: " + loan.getName(),
            occurredOn,
            FinancialReferenceType.LOAN_ENTRY,
            entry.getId())
        : financialAccountService.creditFinancialAccountForReference(
            financialAccountId,
            entryAmount,
            "Recebimento de empréstimo: " + loan.getName(),
            occurredOn,
            FinancialReferenceType.LOAN_ENTRY,
            entry.getId());
    entry.attachFinancialTransaction(transaction.transaction().id());
    loan.addEntry(entry);
    if (installment != null) {
      installment.complete(occurredOn, now);
    }
    loan.refreshStatus(now);
    Loan saved = loanRepository.save(loan);
    return loanResponseDTOFactory.createLoanResponseDTO(
        loanResultFactory.createLoanResult(saved, accountNames(List.of(saved))));
  }

  @Override
  @Transactional
  public void deleteLoan(UUID id) {
    Loan loan = loanRepository.findByIdAndOwnerUserIdForUpdate(
            id,
            currentUserProvider.currentUserId())
        .orElseThrow(() -> new ResourceNotFoundException("Empréstimo não encontrado."));
    loan.softDelete(clock.instant());
  }

  private void createInstallments(
      Loan loan,
      BigDecimal totalAmount,
      BigDecimal requestedInstallmentAmount,
      int installmentCount,
      LocalDate firstDueDate,
      Instant now) {
    BigDecimal regularAmount = totalAmount.divide(
        BigDecimal.valueOf(installmentCount),
        2,
        RoundingMode.DOWN);
    if (requestedInstallmentAmount != null) {
      regularAmount = requestedInstallmentAmount;
    }
    BigDecimal accumulated = BigDecimal.ZERO;
    for (int index = 1; index <= installmentCount; index++) {
      BigDecimal amount = index == installmentCount ? totalAmount.subtract(accumulated) : regularAmount;
      accumulated = accumulated.add(amount);
      loan.addInstallment(new LoanInstallment(
          loan,
          index,
          amount,
          firstDueDate.plusMonths(index - 1L),
          loan.getOwnerUserId(),
          now));
    }
  }

  private BigDecimal entryAmount(Loan loan, BigDecimal amount) {
    if (loan.hasInstallments()) {
      LoanInstallment installment = loan.nextPendingInstallment();
      if (installment == null) {
        throw new IllegalStateException("Este empréstimo já está quitado.");
      }
      return installment.getAmount();
    }
    if (amount == null || amount.signum() <= 0) {
      throw new IllegalArgumentException("Informe um valor maior que zero.");
    }
    return amount;
  }

  private void validateLoan(
      BigDecimal totalAmount,
      Integer installmentCount,
      BigDecimal installmentAmount,
      LocalDate firstDueDate) {
    if (totalAmount.signum() <= 0) {
      throw new IllegalArgumentException("O valor total deve ser maior que zero.");
    }
    boolean hasAnyInstallmentField = installmentCount != null
        || installmentAmount != null
        || firstDueDate != null;
    boolean hasAllInstallmentFields = installmentCount != null
        && installmentAmount != null
        && firstDueDate != null;
    if (hasAnyInstallmentField && !hasAllInstallmentFields) {
      throw new IllegalArgumentException("Informe quantidade, valor e primeiro vencimento das parcelas.");
    }
    if (installmentCount != null) {
      BigDecimal expectedTotal = installmentAmount.multiply(BigDecimal.valueOf(installmentCount));
      BigDecimal difference = expectedTotal.subtract(totalAmount).abs();
      if (difference.compareTo(BigDecimal.valueOf(0.99)) > 0) {
        throw new IllegalArgumentException("A soma das parcelas deve ser compatível com o valor total.");
      }
    }
  }

  private Map<UUID, String> accountNames(List<Loan> loans) {
    Set<UUID> ids = loans.stream()
        .flatMap(loan -> loan.getEntries().stream())
        .map(LoanEntry::getFinancialAccountId)
        .collect(Collectors.toSet());
    return financialAccountService.findFinancialAccountNames(ids);
  }

  private String normalizeRequired(String value) {
    return value.trim();
  }

  private String normalizeOptional(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }
}

package com.orbiafly.api.modules.financialprojection.application.service;

import com.orbiafly.api.infrastructure.security.CurrentUserProvider;
import com.orbiafly.api.modules.creditcard.domain.CreditCardInstallment;
import com.orbiafly.api.modules.creditcard.domain.CreditCardInstallmentStatus;
import com.orbiafly.api.modules.creditcard.domain.CreditCardPurchase;
import com.orbiafly.api.modules.creditcard.infrastructure.persistence.CreditCardPurchaseRepositoryInterface;
import com.orbiafly.api.modules.expectedincome.domain.ExpectedIncome;
import com.orbiafly.api.modules.expectedincome.domain.ExpectedIncomeStatus;
import com.orbiafly.api.modules.expectedincome.infrastructure.persistence.ExpectedIncomeRepositoryInterface;
import com.orbiafly.api.modules.financialaccount.domain.FinancialAccount;
import com.orbiafly.api.modules.financialaccount.infrastructure.persistence.FinancialAccountRepositoryInterface;
import com.orbiafly.api.modules.financialaccount.infrastructure.persistence.FinancialTransactionRepositoryInterface;
import com.orbiafly.api.modules.financialprojection.application.contract.MonthlyFinancialProjectionServiceInterface;
import com.orbiafly.api.modules.financialprojection.application.model.MonthlyFinancialProjectionResult;
import com.orbiafly.api.modules.financialprojection.application.model.ProjectionEventResult;
import com.orbiafly.api.modules.financialprojection.domain.FinancialHealthLevel;
import com.orbiafly.api.modules.financialprojection.domain.ProjectionEventDirection;
import com.orbiafly.api.modules.financialprojection.domain.ProjectionSourceType;
import com.orbiafly.api.modules.financialprojection.web.dto.response.MonthlyFinancialProjectionResponseDTO;
import com.orbiafly.api.modules.financialprojection.web.factory.MonthlyFinancialProjectionResponseDTOFactory;
import com.orbiafly.api.modules.fixedexpense.domain.FixedExpenseOccurrence;
import com.orbiafly.api.modules.fixedexpense.domain.PaymentStatus;
import com.orbiafly.api.modules.fixedexpense.infrastructure.persistence.FixedExpenseOccurrenceRepositoryInterface;
import com.orbiafly.api.modules.installmentexpense.domain.InstallmentExpenseInstallment;
import com.orbiafly.api.modules.installmentexpense.infrastructure.persistence.InstallmentExpenseInstallmentRepositoryInterface;
import com.orbiafly.api.modules.loan.domain.Loan;
import com.orbiafly.api.modules.loan.domain.LoanDirection;
import com.orbiafly.api.modules.loan.domain.LoanInstallment;
import com.orbiafly.api.modules.loan.domain.LoanInstallmentStatus;
import com.orbiafly.api.modules.loan.domain.LoanStatus;
import com.orbiafly.api.modules.loan.infrastructure.persistence.LoanRepositoryInterface;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MonthlyFinancialProjectionServiceImpl implements MonthlyFinancialProjectionServiceInterface {

  private static final BigDecimal ATTENTION_MARGIN_RATIO = BigDecimal.valueOf(0.10);
  private static final BigDecimal COMFORTABLE_MARGIN_RATIO = BigDecimal.valueOf(0.30);

  private final FinancialAccountRepositoryInterface accountRepository;
  private final FinancialTransactionRepositoryInterface transactionRepository;
  private final ExpectedIncomeRepositoryInterface expectedIncomeRepository;
  private final FixedExpenseOccurrenceRepositoryInterface fixedExpenseOccurrenceRepository;
  private final CreditCardPurchaseRepositoryInterface creditCardPurchaseRepository;
  private final InstallmentExpenseInstallmentRepositoryInterface installmentExpenseInstallmentRepository;
  private final LoanRepositoryInterface loanRepository;
  private final MonthlyFinancialProjectionResponseDTOFactory responseDTOFactory;
  private final CurrentUserProvider currentUserProvider;
  private final Clock clock;

  public MonthlyFinancialProjectionServiceImpl(
      FinancialAccountRepositoryInterface accountRepository,
      FinancialTransactionRepositoryInterface transactionRepository,
      ExpectedIncomeRepositoryInterface expectedIncomeRepository,
      FixedExpenseOccurrenceRepositoryInterface fixedExpenseOccurrenceRepository,
      CreditCardPurchaseRepositoryInterface creditCardPurchaseRepository,
      InstallmentExpenseInstallmentRepositoryInterface installmentExpenseInstallmentRepository,
      LoanRepositoryInterface loanRepository,
      MonthlyFinancialProjectionResponseDTOFactory responseDTOFactory,
      CurrentUserProvider currentUserProvider,
      Clock clock) {
    this.accountRepository = accountRepository;
    this.transactionRepository = transactionRepository;
    this.expectedIncomeRepository = expectedIncomeRepository;
    this.fixedExpenseOccurrenceRepository = fixedExpenseOccurrenceRepository;
    this.creditCardPurchaseRepository = creditCardPurchaseRepository;
    this.installmentExpenseInstallmentRepository = installmentExpenseInstallmentRepository;
    this.loanRepository = loanRepository;
    this.responseDTOFactory = responseDTOFactory;
    this.currentUserProvider = currentUserProvider;
    this.clock = clock;
  }

  @Override
  @Transactional
  public MonthlyFinancialProjectionResponseDTO getMonthlyProjection(int year, int month) {
    return responseDTOFactory.createMonthlyFinancialProjectionResponseDTO(
        getMonthlyProjectionResult(year, month));
  }

  @Override
  @Transactional
  public MonthlyFinancialProjectionResult getMonthlyProjectionResult(int year, int month) {
    YearMonth competence = YearMonth.of(year, month);
    UUID ownerUserId = currentUserProvider.currentUserId();
    LocalDate startDate = competence.atDay(1);
    LocalDate endDate = competence.atEndOfMonth();

    BigDecimal currentBalance = currentBalance(ownerUserId);
    List<ProjectionEventDraft> drafts = new ArrayList<>();
    drafts.addAll(expectedIncomeEvents(ownerUserId, startDate, endDate));
    drafts.addAll(fixedExpenseEvents(ownerUserId, year, month, competence));
    drafts.addAll(creditCardEvents(ownerUserId, startDate, endDate));
    drafts.addAll(installmentExpenseEvents(ownerUserId, startDate, endDate));
    drafts.addAll(loanEvents(ownerUserId, startDate, endDate));

    drafts.sort(Comparator
        .comparing(ProjectionEventDraft::date)
        .thenComparing(event -> event.direction() == ProjectionEventDirection.OUTFLOW ? 0 : 1)
        .thenComparing(ProjectionEventDraft::description));

    BigDecimal runningBalance = currentBalance;
    BigDecimal minimumBalance = currentBalance;
    LocalDate minimumDate = startDate;
    List<ProjectionEventResult> events = new ArrayList<>();
    for (ProjectionEventDraft draft : drafts) {
      runningBalance = draft.direction() == ProjectionEventDirection.INFLOW
          ? runningBalance.add(draft.amount())
          : runningBalance.subtract(draft.amount());
      if (runningBalance.compareTo(minimumBalance) < 0) {
        minimumBalance = runningBalance;
        minimumDate = draft.date();
      }
      events.add(new ProjectionEventResult(
          draft.date(),
          draft.description(),
          draft.amount(),
          draft.direction(),
          draft.sourceType(),
          draft.sourceId(),
          runningBalance));
    }

    BigDecimal expectedIncomeTotal = drafts.stream()
        .filter(event -> event.direction() == ProjectionEventDirection.INFLOW)
        .map(ProjectionEventDraft::amount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal expectedExpenseTotal = drafts.stream()
        .filter(event -> event.direction() == ProjectionEventDirection.OUTFLOW)
        .map(ProjectionEventDraft::amount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    FinancialHealthLevel healthLevel = healthLevel(minimumBalance, expectedExpenseTotal);
    return new MonthlyFinancialProjectionResult(
        year,
        month,
        currentBalance,
        expectedIncomeTotal,
        expectedExpenseTotal,
        runningBalance,
        minimumBalance,
        minimumDate,
        healthLevel,
        insight(healthLevel, minimumBalance, minimumDate),
        events);
  }

  private BigDecimal currentBalance(UUID ownerUserId) {
    Map<UUID, BigDecimal> deltas = new HashMap<>();
    transactionRepository.findBalanceDeltas(ownerUserId).forEach(projection ->
        deltas.put(projection.getAccountId(), projection.getBalanceDelta()));
    return accountRepository.findAllByOwnerUserIdAndActiveTrueOrderByNameAsc(ownerUserId).stream()
        .map(account -> account.getOpeningBalance().add(deltas.getOrDefault(account.getId(), BigDecimal.ZERO)))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private List<ProjectionEventDraft> expectedIncomeEvents(
      UUID ownerUserId,
      LocalDate startDate,
      LocalDate endDate) {
    return expectedIncomeRepository
        .findAllByOwnerUserIdAndStatusAndDeletedAtIsNullAndExpectedDateBetweenOrderByExpectedDateAscDescriptionAsc(
            ownerUserId,
            ExpectedIncomeStatus.PENDING,
            startDate,
            endDate)
        .stream()
        .map(income -> new ProjectionEventDraft(
            income.getExpectedDate(),
            income.getDescription(),
            income.getAmount(),
            ProjectionEventDirection.INFLOW,
            ProjectionSourceType.EXPECTED_INCOME,
            income.getId()))
        .toList();
  }

  private List<ProjectionEventDraft> fixedExpenseEvents(
      UUID ownerUserId,
      int year,
      int month,
      YearMonth competence) {
    fixedExpenseOccurrenceRepository.materializeActiveExpenses(ownerUserId, year, month);
    return fixedExpenseOccurrenceRepository.findMonthly(ownerUserId, year, month).stream()
        .filter(occurrence -> occurrence.getStatus() == PaymentStatus.PENDING)
        .map(occurrence -> new ProjectionEventDraft(
            dueDate(competence, occurrence.getDueDay()),
            occurrence.getName(),
            occurrence.getAmount(),
            ProjectionEventDirection.OUTFLOW,
            ProjectionSourceType.FIXED_EXPENSE,
            occurrence.getId()))
        .toList();
  }

  private List<ProjectionEventDraft> creditCardEvents(
      UUID ownerUserId,
      LocalDate startDate,
      LocalDate endDate) {
    List<ProjectionEventDraft> events = new ArrayList<>();
    for (CreditCardPurchase purchase : creditCardPurchaseRepository
        .findAllByOwnerUserIdAndDeletedAtIsNullOrderByPurchaseDateDescCreatedAtDesc(ownerUserId)) {
      for (CreditCardInstallment installment : purchase.getInstallments()) {
        if (installment.getStatus() == CreditCardInstallmentStatus.PENDING
            && !installment.getDueDate().isBefore(startDate)
            && !installment.getDueDate().isAfter(endDate)) {
          events.add(new ProjectionEventDraft(
              installment.getDueDate(),
              purchase.getDescription(),
              installment.getAmount(),
              ProjectionEventDirection.OUTFLOW,
              ProjectionSourceType.CREDIT_CARD_INSTALLMENT,
              installment.getId()));
        }
      }
    }
    return events;
  }

  private List<ProjectionEventDraft> installmentExpenseEvents(
      UUID ownerUserId,
      LocalDate startDate,
      LocalDate endDate) {
    return installmentExpenseInstallmentRepository
        .findMonthlyPending(ownerUserId, PaymentStatus.PENDING, startDate, endDate)
        .stream()
        .map(installment -> new ProjectionEventDraft(
            installment.getDueDate(),
            installment.getPurchase().getDescription(),
            installment.getAmount(),
            ProjectionEventDirection.OUTFLOW,
            ProjectionSourceType.INSTALLMENT_EXPENSE,
            installment.getId()))
        .toList();
  }

  private List<ProjectionEventDraft> loanEvents(
      UUID ownerUserId,
      LocalDate startDate,
      LocalDate endDate) {
    List<ProjectionEventDraft> events = new ArrayList<>();
    for (Loan loan : loanRepository.findAllByOwnerUserIdAndDeletedAtIsNullOrderByStatusAscStartDateDescCreatedAtDesc(
        ownerUserId)) {
      if (loan.getStatus() != LoanStatus.ACTIVE) {
        continue;
      }
      for (LoanInstallment installment : loan.getInstallments()) {
        if (installment.getStatus() == LoanInstallmentStatus.PENDING
            && !installment.getDueDate().isBefore(startDate)
            && !installment.getDueDate().isAfter(endDate)) {
          ProjectionEventDirection direction = loan.getDirection() == LoanDirection.LENT
              ? ProjectionEventDirection.INFLOW
              : ProjectionEventDirection.OUTFLOW;
          events.add(new ProjectionEventDraft(
              installment.getDueDate(),
              loan.getName(),
              installment.getAmount(),
              direction,
              ProjectionSourceType.LOAN_INSTALLMENT,
              installment.getId()));
        }
      }
    }
    return events;
  }

  private LocalDate dueDate(YearMonth competence, int dueDay) {
    return competence.atDay(Math.min(dueDay, competence.lengthOfMonth()));
  }

  private FinancialHealthLevel healthLevel(BigDecimal minimumBalance, BigDecimal expectedExpenseTotal) {
    if (minimumBalance.signum() < 0) {
      return FinancialHealthLevel.CRITICAL;
    }
    if (expectedExpenseTotal.signum() == 0) {
      return minimumBalance.signum() > 0 ? FinancialHealthLevel.COMFORTABLE : FinancialHealthLevel.HEALTHY;
    }
    BigDecimal marginRatio = minimumBalance.divide(expectedExpenseTotal, 4, RoundingMode.HALF_UP);
    if (marginRatio.compareTo(ATTENTION_MARGIN_RATIO) < 0) {
      return FinancialHealthLevel.ATTENTION;
    }
    if (marginRatio.compareTo(COMFORTABLE_MARGIN_RATIO) >= 0) {
      return FinancialHealthLevel.COMFORTABLE;
    }
    return FinancialHealthLevel.HEALTHY;
  }

  private String insight(FinancialHealthLevel healthLevel, BigDecimal minimumBalance, LocalDate minimumDate) {
    if (healthLevel == FinancialHealthLevel.CRITICAL) {
      return "Seu saldo projetado pode ficar negativo em " + formatDate(minimumDate) + ".";
    }
    return "Seu menor saldo previsto é de " + minimumBalance + " em " + formatDate(minimumDate) + ".";
  }

  private String formatDate(LocalDate date) {
    return "%02d/%02d".formatted(date.getDayOfMonth(), date.getMonthValue());
  }

  private record ProjectionEventDraft(
      LocalDate date,
      String description,
      BigDecimal amount,
      ProjectionEventDirection direction,
      ProjectionSourceType sourceType,
      UUID sourceId) {
  }
}

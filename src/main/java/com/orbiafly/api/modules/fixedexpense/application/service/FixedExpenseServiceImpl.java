package com.orbiafly.api.modules.fixedexpense.application.service;

import com.orbiafly.api.modules.auth.application.contract.CurrentUserServiceInterface;
import com.orbiafly.api.modules.financialaccount.application.contract.FinancialAccountServiceInterface;
import com.orbiafly.api.modules.financialaccount.application.model.AccountTransactionResult;
import com.orbiafly.api.modules.financialaccount.application.model.FinancialAccountResult;
import com.orbiafly.api.modules.financialaccount.application.model.FinancialTransactionResult;
import com.orbiafly.api.modules.financialaccount.domain.FinancialReferenceType;
import com.orbiafly.api.modules.fixedexpense.application.ResourceNotFoundException;
import com.orbiafly.api.modules.fixedexpense.application.contract.FixedExpenseServiceInterface;
import com.orbiafly.api.modules.fixedexpense.application.factory.CategoryResultFactory;
import com.orbiafly.api.modules.fixedexpense.application.factory.FixedExpenseResultFactory;
import com.orbiafly.api.modules.fixedexpense.application.factory.MonthlyExpenseResultFactory;
import com.orbiafly.api.modules.fixedexpense.application.model.CategoryResult;
import com.orbiafly.api.modules.fixedexpense.application.model.FixedExpensePaymentResult;
import com.orbiafly.api.modules.fixedexpense.application.model.FixedExpenseResult;
import com.orbiafly.api.modules.fixedexpense.application.model.MonthlyExpenseResult;
import com.orbiafly.api.modules.fixedexpense.application.model.MonthlyExpenseSummary;
import com.orbiafly.api.modules.fixedexpense.application.model.MonthlyExpensesResult;
import com.orbiafly.api.modules.fixedexpense.domain.Category;
import com.orbiafly.api.modules.fixedexpense.domain.FixedExpense;
import com.orbiafly.api.modules.fixedexpense.domain.FixedExpenseOccurrence;
import com.orbiafly.api.modules.fixedexpense.domain.PaymentStatus;
import com.orbiafly.api.modules.fixedexpense.infrastructure.persistence.CategoryRepositoryInterface;
import com.orbiafly.api.modules.fixedexpense.infrastructure.persistence.FixedExpenseOccurrenceRepositoryInterface;
import com.orbiafly.api.modules.fixedexpense.infrastructure.persistence.FixedExpenseRepositoryInterface;
import com.orbiafly.api.modules.fixedexpense.web.dto.response.CategoryResponseDTO;
import com.orbiafly.api.modules.fixedexpense.web.dto.response.FixedExpensePaymentResponseDTO;
import com.orbiafly.api.modules.fixedexpense.web.dto.response.FixedExpenseResponseDTO;
import com.orbiafly.api.modules.fixedexpense.web.dto.response.MonthlyExpenseResponseDTO;
import com.orbiafly.api.modules.fixedexpense.web.dto.response.MonthlyExpensesResponseDTO;
import com.orbiafly.api.modules.fixedexpense.web.factory.CategoryResponseDTOFactory;
import com.orbiafly.api.modules.fixedexpense.web.factory.FixedExpensePaymentResponseDTOFactory;
import com.orbiafly.api.modules.fixedexpense.web.factory.FixedExpenseResponseDTOFactory;
import com.orbiafly.api.modules.fixedexpense.web.factory.MonthlyExpenseResponseDTOFactory;
import com.orbiafly.api.modules.fixedexpense.web.factory.MonthlyExpensesResponseDTOFactory;
import com.orbiafly.api.modules.user.domain.User;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FixedExpenseServiceImpl implements FixedExpenseServiceInterface {

  private static final int MIN_YEAR = 2000;
  private static final int MAX_YEAR = 2200;

  private final CategoryRepositoryInterface categoryRepository;
  private final FixedExpenseRepositoryInterface fixedExpenseRepository;
  private final FixedExpenseOccurrenceRepositoryInterface occurrenceRepository;
  private final FinancialAccountServiceInterface financialAccountService;
  private final CurrentUserServiceInterface currentUserService;
  private final CategoryResultFactory categoryResultFactory;
  private final FixedExpenseResultFactory fixedExpenseResultFactory;
  private final MonthlyExpenseResultFactory monthlyExpenseResultFactory;
  private final CategoryResponseDTOFactory categoryResponseDTOFactory;
  private final FixedExpenseResponseDTOFactory fixedExpenseResponseDTOFactory;
  private final MonthlyExpenseResponseDTOFactory monthlyExpenseResponseDTOFactory;
  private final MonthlyExpensesResponseDTOFactory monthlyExpensesResponseDTOFactory;
  private final FixedExpensePaymentResponseDTOFactory paymentResponseDTOFactory;
  private final Clock clock;

  public FixedExpenseServiceImpl(
      CategoryRepositoryInterface categoryRepository,
      FixedExpenseRepositoryInterface fixedExpenseRepository,
      FixedExpenseOccurrenceRepositoryInterface occurrenceRepository,
      FinancialAccountServiceInterface financialAccountService,
      CurrentUserServiceInterface currentUserService,
      CategoryResultFactory categoryResultFactory,
      FixedExpenseResultFactory fixedExpenseResultFactory,
      MonthlyExpenseResultFactory monthlyExpenseResultFactory,
      CategoryResponseDTOFactory categoryResponseDTOFactory,
      FixedExpenseResponseDTOFactory fixedExpenseResponseDTOFactory,
      MonthlyExpenseResponseDTOFactory monthlyExpenseResponseDTOFactory,
      MonthlyExpensesResponseDTOFactory monthlyExpensesResponseDTOFactory,
      FixedExpensePaymentResponseDTOFactory paymentResponseDTOFactory,
      Clock clock) {
    this.categoryRepository = categoryRepository;
    this.fixedExpenseRepository = fixedExpenseRepository;
    this.occurrenceRepository = occurrenceRepository;
    this.financialAccountService = financialAccountService;
    this.currentUserService = currentUserService;
    this.categoryResultFactory = categoryResultFactory;
    this.fixedExpenseResultFactory = fixedExpenseResultFactory;
    this.monthlyExpenseResultFactory = monthlyExpenseResultFactory;
    this.categoryResponseDTOFactory = categoryResponseDTOFactory;
    this.fixedExpenseResponseDTOFactory = fixedExpenseResponseDTOFactory;
    this.monthlyExpenseResponseDTOFactory = monthlyExpenseResponseDTOFactory;
    this.monthlyExpensesResponseDTOFactory = monthlyExpensesResponseDTOFactory;
    this.paymentResponseDTOFactory = paymentResponseDTOFactory;
    this.clock = clock;
  }

  @Override
  @Transactional(readOnly = true)
  public List<CategoryResponseDTO> listFixedExpenseCategories() {
    return categoryRepository.findAllByOrderByNameAsc().stream()
        .map(categoryResultFactory::createCategoryResult)
        .map(categoryResponseDTOFactory::createCategoryResponseDTO)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<FixedExpenseResponseDTO> listFixedExpenses() {
    return fixedExpenseRepository.findAllByOwnerIdOrderByNameAsc(
            currentUserService.getCurrentUser().getId()).stream()
        .map(fixedExpenseResultFactory::createFixedExpenseResult)
        .map(fixedExpenseResponseDTOFactory::createFixedExpenseResponseDTO)
        .toList();
  }

  @Override
  @Transactional
  public FixedExpenseResponseDTO createFixedExpense(
      String name,
      BigDecimal defaultAmount,
      int dueDay,
      UUID categoryId) {
    Category category = findCategory(categoryId);
    Instant now = clock.instant();
    FixedExpense expense = new FixedExpense(
        currentUserService.getCurrentUser(),
        normalizeName(name),
        defaultAmount,
        dueDay,
        category,
        now);
    FixedExpenseResult result = fixedExpenseResultFactory.createFixedExpenseResult(
        fixedExpenseRepository.save(expense));
    return fixedExpenseResponseDTOFactory.createFixedExpenseResponseDTO(result);
  }

  @Override
  @Transactional
  public FixedExpenseResponseDTO updateFixedExpense(
      UUID id,
      String name,
      BigDecimal defaultAmount,
      int dueDay,
      UUID categoryId,
      boolean active) {
    FixedExpense expense = findExpense(id);
    expense.updateFixedExpense(
        normalizeName(name),
        defaultAmount,
        dueDay,
        findCategory(categoryId),
        active,
        clock.instant());
    FixedExpenseResult result = fixedExpenseResultFactory.createFixedExpenseResult(expense);
    return fixedExpenseResponseDTOFactory.createFixedExpenseResponseDTO(result);
  }

  @Override
  @Transactional
  public MonthlyExpensesResponseDTO getMonthlyFixedExpenses(int year, int month) {
    validatePeriod(year, month);
    occurrenceRepository.materializeActiveExpenses(year, month, currentUserService.getCurrentUser().getId());
    MonthlyExpensesResult result = createMonthlyExpensesResult(
        year,
        month,
        occurrenceRepository.findMonthly(year, month, currentUserService.getCurrentUser().getId()));
    return monthlyExpensesResponseDTOFactory.createMonthlyExpensesResponseDTO(result);
  }

  @Override
  @Transactional
  public MonthlyExpenseResponseDTO updateFixedExpenseOccurrenceAmount(
      UUID occurrenceId,
      BigDecimal amount) {
    FixedExpenseOccurrence occurrence = findOccurrenceForUpdate(occurrenceId);
    if (occurrence.getStatus() == PaymentStatus.PAID) {
      throw new IllegalStateException(
          "Volte a despesa para pendente antes de alterar o valor mensal.");
    }
    occurrence.updateAmount(amount, clock.instant());
    return monthlyExpenseResponseDTOFactory.createMonthlyExpenseResponseDTO(
        createMonthlyExpenseResult(occurrence));
  }

  @Override
  @Transactional
  public MonthlyExpenseResponseDTO updateFixedExpenseOccurrenceStatus(
      UUID occurrenceId,
      PaymentStatus status) {
    FixedExpenseOccurrence occurrence = findOccurrenceForUpdate(occurrenceId);
    if (occurrence.getStatus() == status) {
      return monthlyExpenseResponseDTOFactory.createMonthlyExpenseResponseDTO(
          createMonthlyExpenseResult(occurrence));
    }
    if (status == PaymentStatus.PAID) {
      if (financialAccountService.hasActiveFinancialAccounts()) {
        throw new IllegalStateException("Selecione uma conta para registrar o pagamento.");
      }
      occurrence.updateStatus(PaymentStatus.PAID, clock.instant());
      return monthlyExpenseResponseDTOFactory.createMonthlyExpenseResponseDTO(
          createMonthlyExpenseResult(occurrence));
    }
    if (occurrence.getPaymentTransactionId() != null) {
      financialAccountService.reverseFinancialAccountDebit(
          occurrence.getPaymentTransactionId(),
          LocalDate.now(clock),
          "Estorno - " + occurrence.getName());
    }
    occurrence.updateStatus(PaymentStatus.PENDING, clock.instant());
    return monthlyExpenseResponseDTOFactory.createMonthlyExpenseResponseDTO(
        createMonthlyExpenseResult(occurrence));
  }

  @Override
  @Transactional
  public FixedExpensePaymentResponseDTO registerFixedExpenseOccurrencePayment(
      UUID occurrenceId,
      UUID financialAccountId,
      LocalDate paymentDate) {
    FixedExpenseOccurrence occurrence = findOccurrenceForUpdate(occurrenceId);
    if (occurrence.getStatus() == PaymentStatus.PAID) {
      if (Objects.equals(occurrence.getFinancialAccountId(), financialAccountId)
          && occurrence.getPaymentTransactionId() != null) {
        FinancialAccountResult account = financialAccountService.getFinancialAccountResult(
            financialAccountId);
        FinancialTransactionResult transaction = financialAccountService.getFinancialTransaction(
            occurrence.getPaymentTransactionId());
        FixedExpensePaymentResult payment = createFixedExpensePaymentResult(
            occurrence,
            account,
            transaction);
        return paymentResponseDTOFactory.createFixedExpensePaymentResponseDTO(payment);
      }
      throw new IllegalStateException("Esta despesa já possui um pagamento registrado.");
    }
    AccountTransactionResult result = financialAccountService.debitFinancialAccountForReference(
        financialAccountId,
        occurrence.getAmount(),
        "Pagamento - " + occurrence.getName(),
        paymentDate,
        FinancialReferenceType.FIXED_EXPENSE_OCCURRENCE,
        occurrence.getId());
    occurrence.registerPayment(
        financialAccountId,
        result.transaction().id(),
        clock.instant());
    FixedExpensePaymentResult payment = createFixedExpensePaymentResult(
        occurrence,
        result.financialAccount(),
        result.transaction());
    return paymentResponseDTOFactory.createFixedExpensePaymentResponseDTO(payment);
  }

  private Category findCategory(UUID id) {
    return categoryRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada."));
  }

  private FixedExpense findExpense(UUID id) {
    return fixedExpenseRepository.findByIdAndOwnerId(id, currentUserService.getCurrentUser().getId())
        .orElseThrow(() -> new ResourceNotFoundException("Despesa fixa não encontrada."));
  }

  private FixedExpenseOccurrence findOccurrenceForUpdate(UUID id) {
    return occurrenceRepository.findByIdAndOwnerIdForUpdate(id, currentUserService.getCurrentUser().getId())
        .orElseThrow(() -> new ResourceNotFoundException("Ocorrência mensal não encontrada."));
  }

  private void validatePeriod(int year, int month) {
    if (year < MIN_YEAR || year > MAX_YEAR) {
      throw new IllegalArgumentException("O ano deve estar entre 2000 e 2200.");
    }
    YearMonth.of(year, month);
  }

  private String normalizeName(String name) {
    return name.trim();
  }

  private MonthlyExpenseResult createMonthlyExpenseResult(FixedExpenseOccurrence occurrence) {
    Map<UUID, String> accountNames = occurrence.getFinancialAccountId() == null
        ? Map.of()
        : financialAccountService.findFinancialAccountNames(Set.of(occurrence.getFinancialAccountId()));
    return createMonthlyExpenseResult(occurrence, accountNames);
  }

  private MonthlyExpenseResult createMonthlyExpenseResult(
      FixedExpenseOccurrence occurrence,
      Map<UUID, String> accountNames) {
    return monthlyExpenseResultFactory.createMonthlyExpenseResult(occurrence, accountNames);
  }

  private MonthlyExpensesResult createMonthlyExpensesResult(
      int year,
      int month,
      List<FixedExpenseOccurrence> occurrences) {
    Set<UUID> accountIds = occurrences.stream()
        .map(FixedExpenseOccurrence::getFinancialAccountId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    Map<UUID, String> accountNames = financialAccountService.findFinancialAccountNames(accountIds);
    List<MonthlyExpenseResult> items = occurrences.stream()
        .map(occurrence -> createMonthlyExpenseResult(occurrence, accountNames))
        .toList();
    BigDecimal paidTotal = totalByStatus(items, PaymentStatus.PAID);
    BigDecimal pendingTotal = totalByStatus(items, PaymentStatus.PENDING);
    int paidCount = countByStatus(items, PaymentStatus.PAID);
    int pendingCount = countByStatus(items, PaymentStatus.PENDING);
    BigDecimal total = paidTotal.add(pendingTotal);
    MonthlyExpenseSummary summary = new MonthlyExpenseSummary(
        total,
        paidTotal,
        pendingTotal,
        items.size(),
        paidCount,
        pendingCount,
        paidPercentage(paidTotal, total));
    return new MonthlyExpensesResult(year, month, summary, items);
  }

  private BigDecimal paidPercentage(BigDecimal paidTotal, BigDecimal total) {
    if (total.signum() == 0) {
      return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
    return paidTotal
        .multiply(BigDecimal.valueOf(100))
        .divide(total, 2, RoundingMode.HALF_UP);
  }

  private BigDecimal totalByStatus(List<MonthlyExpenseResult> items, PaymentStatus status) {
    return items.stream()
        .filter(item -> item.status() == status)
        .map(MonthlyExpenseResult::amount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private int countByStatus(List<MonthlyExpenseResult> items, PaymentStatus status) {
    return (int) items.stream().filter(item -> item.status() == status).count();
  }

  private FixedExpensePaymentResult createFixedExpensePaymentResult(
      FixedExpenseOccurrence occurrence,
      FinancialAccountResult account,
      FinancialTransactionResult transaction) {
    MonthlyExpenseResult occurrenceView = createMonthlyExpenseResult(
        occurrence,
        Map.of(account.id(), account.name()));
    return new FixedExpensePaymentResult(
        occurrenceView,
        transaction,
        account,
        account.currentBalance(),
        account.currentBalance().signum() < 0);
  }
}

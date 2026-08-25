package com.orbiafly.api.modules.fixedexpense.application;

import com.orbiafly.api.modules.financialaccount.application.AccountTransactionResult;
import com.orbiafly.api.modules.financialaccount.application.FinancialAccountService;
import com.orbiafly.api.modules.financialaccount.application.FinancialAccountView;
import com.orbiafly.api.modules.financialaccount.application.FinancialTransactionView;
import com.orbiafly.api.modules.financialaccount.domain.FinancialReferenceType;
import com.orbiafly.api.modules.fixedexpense.domain.Category;
import com.orbiafly.api.modules.fixedexpense.domain.FixedExpense;
import com.orbiafly.api.modules.fixedexpense.domain.FixedExpenseOccurrence;
import com.orbiafly.api.modules.fixedexpense.domain.PaymentStatus;
import com.orbiafly.api.modules.fixedexpense.infrastructure.persistence.CategoryRepository;
import com.orbiafly.api.modules.fixedexpense.infrastructure.persistence.FixedExpenseOccurrenceRepository;
import com.orbiafly.api.modules.fixedexpense.infrastructure.persistence.FixedExpenseRepository;
import java.math.BigDecimal;
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
public class FixedExpenseService {

  private static final int MIN_YEAR = 2000;
  private static final int MAX_YEAR = 2200;

  private final CategoryRepository categoryRepository;
  private final FixedExpenseRepository fixedExpenseRepository;
  private final FixedExpenseOccurrenceRepository occurrenceRepository;
  private final FinancialAccountService financialAccountService;
  private final Clock clock;

  public FixedExpenseService(
      CategoryRepository categoryRepository,
      FixedExpenseRepository fixedExpenseRepository,
      FixedExpenseOccurrenceRepository occurrenceRepository,
      FinancialAccountService financialAccountService,
      Clock clock) {
    this.categoryRepository = categoryRepository;
    this.fixedExpenseRepository = fixedExpenseRepository;
    this.occurrenceRepository = occurrenceRepository;
    this.financialAccountService = financialAccountService;
    this.clock = clock;
  }

  @Transactional(readOnly = true)
  public List<CategoryView> listCategories() {
    return categoryRepository.findAllByOrderByNameAsc().stream()
        .map(category -> new CategoryView(category.getId(), category.getName()))
        .toList();
  }

  @Transactional(readOnly = true)
  public List<FixedExpenseView> listFixedExpenses() {
    return fixedExpenseRepository.findAllByOrderByNameAsc().stream()
        .map(this::toView)
        .toList();
  }

  @Transactional
  public FixedExpenseView create(
      String name,
      BigDecimal defaultAmount,
      int dueDay,
      UUID categoryId) {
    Category category = findCategory(categoryId);
    Instant now = clock.instant();
    FixedExpense expense = new FixedExpense(
        normalizeName(name),
        defaultAmount,
        dueDay,
        category,
        now);
    return toView(fixedExpenseRepository.save(expense));
  }

  @Transactional
  public FixedExpenseView update(
      UUID id,
      String name,
      BigDecimal defaultAmount,
      int dueDay,
      UUID categoryId,
      boolean active) {
    FixedExpense expense = findExpense(id);
    expense.update(
        normalizeName(name),
        defaultAmount,
        dueDay,
        findCategory(categoryId),
        active,
        clock.instant());
    return toView(expense);
  }

  @Transactional
  public MonthlyExpensesView getMonthly(int year, int month) {
    validatePeriod(year, month);
    occurrenceRepository.materializeActiveExpenses(year, month);
    return toMonthlyView(year, month, occurrenceRepository.findMonthly(year, month));
  }

  @Transactional
  public MonthlyExpenseView updateOccurrenceAmount(UUID occurrenceId, BigDecimal amount) {
    FixedExpenseOccurrence occurrence = findOccurrenceForUpdate(occurrenceId);
    if (occurrence.getStatus() == PaymentStatus.PAID) {
      throw new IllegalStateException(
          "Volte a despesa para pendente antes de alterar o valor mensal.");
    }
    occurrence.updateAmount(amount, clock.instant());
    return toMonthlyItem(occurrence);
  }

  @Transactional
  public MonthlyExpenseView updateOccurrenceStatus(UUID occurrenceId, PaymentStatus status) {
    FixedExpenseOccurrence occurrence = findOccurrenceForUpdate(occurrenceId);
    if (occurrence.getStatus() == status) {
      return toMonthlyItem(occurrence);
    }
    if (status == PaymentStatus.PAID) {
      if (financialAccountService.hasActiveAccounts()) {
        throw new IllegalStateException("Selecione uma conta para registrar o pagamento.");
      }
      occurrence.updateStatus(PaymentStatus.PAID, clock.instant());
      return toMonthlyItem(occurrence);
    }
    if (occurrence.getPaymentTransactionId() != null) {
      financialAccountService.reverseDebit(
          occurrence.getPaymentTransactionId(),
          LocalDate.now(clock),
          "Estorno - " + occurrence.getName());
    }
    occurrence.updateStatus(PaymentStatus.PENDING, clock.instant());
    return toMonthlyItem(occurrence);
  }

  @Transactional
  public FixedExpensePaymentView registerPayment(
      UUID occurrenceId,
      UUID financialAccountId,
      LocalDate paymentDate) {
    FixedExpenseOccurrence occurrence = findOccurrenceForUpdate(occurrenceId);
    if (occurrence.getStatus() == PaymentStatus.PAID) {
      if (Objects.equals(occurrence.getFinancialAccountId(), financialAccountId)
          && occurrence.getPaymentTransactionId() != null) {
        FinancialAccountView account = financialAccountService.get(financialAccountId);
        FinancialTransactionView transaction = financialAccountService.getTransaction(
            occurrence.getPaymentTransactionId());
        return paymentView(occurrence, account, transaction);
      }
      throw new IllegalStateException("Esta despesa já possui um pagamento registrado.");
    }
    AccountTransactionResult result = financialAccountService.debitForReference(
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
    return paymentView(occurrence, result.financialAccount(), result.transaction());
  }

  private Category findCategory(UUID id) {
    return categoryRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada."));
  }

  private FixedExpense findExpense(UUID id) {
    return fixedExpenseRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Despesa fixa não encontrada."));
  }

  private FixedExpenseOccurrence findOccurrence(UUID id) {
    return occurrenceRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Ocorrência mensal não encontrada."));
  }

  private FixedExpenseOccurrence findOccurrenceForUpdate(UUID id) {
    return occurrenceRepository.findByIdForUpdate(id)
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

  private FixedExpenseView toView(FixedExpense expense) {
    return new FixedExpenseView(
        expense.getId(),
        expense.getName(),
        expense.getDefaultAmount(),
        expense.getDueDay(),
        expense.getCategory().getId(),
        expense.getCategory().getName(),
        expense.isActive(),
        expense.getCreatedAt(),
        expense.getUpdatedAt());
  }

  private MonthlyExpenseView toMonthlyItem(FixedExpenseOccurrence occurrence) {
    Map<UUID, String> accountNames = occurrence.getFinancialAccountId() == null
        ? Map.of()
        : financialAccountService.findNames(Set.of(occurrence.getFinancialAccountId()));
    return toMonthlyItem(occurrence, accountNames);
  }

  private MonthlyExpenseView toMonthlyItem(
      FixedExpenseOccurrence occurrence,
      Map<UUID, String> accountNames) {
    FixedExpense expense = occurrence.getFixedExpense();
    String accountName = occurrence.getFinancialAccountId() == null
        ? null
        : accountNames.get(occurrence.getFinancialAccountId());
    return new MonthlyExpenseView(
        occurrence.getId(),
        expense.getId(),
        occurrence.getName(),
        occurrence.getAmount(),
        expense.getDefaultAmount(),
        occurrence.getDueDay(),
        occurrence.getCategoryId(),
        occurrence.getCategoryName(),
        occurrence.getStatus(),
        occurrence.getPaidAt(),
        occurrence.getFinancialAccountId(),
        accountName,
        expense.isActive());
  }

  private MonthlyExpensesView toMonthlyView(
      int year,
      int month,
      List<FixedExpenseOccurrence> occurrences) {
    Set<UUID> accountIds = occurrences.stream()
        .map(FixedExpenseOccurrence::getFinancialAccountId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    Map<UUID, String> accountNames = financialAccountService.findNames(accountIds);
    List<MonthlyExpenseView> items = occurrences.stream()
        .map(occurrence -> toMonthlyItem(occurrence, accountNames))
        .toList();
    BigDecimal paidTotal = totalByStatus(items, PaymentStatus.PAID);
    BigDecimal pendingTotal = totalByStatus(items, PaymentStatus.PENDING);
    int paidCount = countByStatus(items, PaymentStatus.PAID);
    int pendingCount = countByStatus(items, PaymentStatus.PENDING);
    MonthlyExpenseSummary summary = new MonthlyExpenseSummary(
        paidTotal.add(pendingTotal),
        paidTotal,
        pendingTotal,
        paidCount,
        pendingCount);
    return new MonthlyExpensesView(year, month, summary, items);
  }

  private BigDecimal totalByStatus(List<MonthlyExpenseView> items, PaymentStatus status) {
    return items.stream()
        .filter(item -> item.status() == status)
        .map(MonthlyExpenseView::amount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private int countByStatus(List<MonthlyExpenseView> items, PaymentStatus status) {
    return (int) items.stream().filter(item -> item.status() == status).count();
  }

  private FixedExpensePaymentView paymentView(
      FixedExpenseOccurrence occurrence,
      FinancialAccountView account,
      FinancialTransactionView transaction) {
    MonthlyExpenseView occurrenceView = toMonthlyItem(
        occurrence,
        Map.of(account.id(), account.name()));
    return new FixedExpensePaymentView(
        occurrenceView,
        transaction,
        account,
        account.currentBalance(),
        account.currentBalance().signum() < 0);
  }
}

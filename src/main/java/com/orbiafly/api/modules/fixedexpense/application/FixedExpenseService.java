package com.orbiafly.api.modules.fixedexpense.application;

import com.orbiafly.api.modules.fixedexpense.domain.Category;
import com.orbiafly.api.modules.fixedexpense.domain.FixedExpense;
import com.orbiafly.api.modules.fixedexpense.domain.FixedExpenseOccurrence;
import com.orbiafly.api.modules.fixedexpense.domain.PaymentStatus;
import com.orbiafly.api.modules.fixedexpense.infrastructure.persistence.CategoryRepository;
import com.orbiafly.api.modules.fixedexpense.infrastructure.persistence.FixedExpenseOccurrenceRepository;
import com.orbiafly.api.modules.fixedexpense.infrastructure.persistence.FixedExpenseRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FixedExpenseService {

  private static final int MIN_YEAR = 2000;
  private static final int MAX_YEAR = 2200;

  private final CategoryRepository categoryRepository;
  private final FixedExpenseRepository fixedExpenseRepository;
  private final FixedExpenseOccurrenceRepository occurrenceRepository;

  public FixedExpenseService(
      CategoryRepository categoryRepository,
      FixedExpenseRepository fixedExpenseRepository,
      FixedExpenseOccurrenceRepository occurrenceRepository) {
    this.categoryRepository = categoryRepository;
    this.fixedExpenseRepository = fixedExpenseRepository;
    this.occurrenceRepository = occurrenceRepository;
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
    Instant now = Instant.now();
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
        Instant.now());
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
    FixedExpenseOccurrence occurrence = findOccurrence(occurrenceId);
    occurrence.updateAmount(amount, Instant.now());
    return toMonthlyItem(occurrence);
  }

  @Transactional
  public MonthlyExpenseView updateOccurrenceStatus(UUID occurrenceId, PaymentStatus status) {
    FixedExpenseOccurrence occurrence = findOccurrence(occurrenceId);
    occurrence.updateStatus(status, Instant.now());
    return toMonthlyItem(occurrence);
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
    FixedExpense expense = occurrence.getFixedExpense();
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
        expense.isActive());
  }

  private MonthlyExpensesView toMonthlyView(
      int year,
      int month,
      List<FixedExpenseOccurrence> occurrences) {
    List<MonthlyExpenseView> items = occurrences.stream().map(this::toMonthlyItem).toList();
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
}

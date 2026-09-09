package com.orbiafly.api.modules.oneoffexpense.application.service;

import com.orbiafly.api.infrastructure.security.CurrentUserProvider;
import com.orbiafly.api.modules.financialaccount.application.contract.FinancialAccountServiceInterface;
import com.orbiafly.api.modules.financialaccount.application.model.AccountTransactionResult;
import com.orbiafly.api.modules.financialaccount.application.model.FinancialAccountResult;
import com.orbiafly.api.modules.financialaccount.application.model.FinancialTransactionResult;
import com.orbiafly.api.modules.financialaccount.domain.FinancialReferenceType;
import com.orbiafly.api.modules.fixedexpense.application.ResourceNotFoundException;
import com.orbiafly.api.modules.fixedexpense.application.model.MonthlyExpenseSummary;
import com.orbiafly.api.modules.fixedexpense.domain.Category;
import com.orbiafly.api.modules.fixedexpense.domain.PaymentStatus;
import com.orbiafly.api.modules.fixedexpense.infrastructure.persistence.CategoryRepositoryInterface;
import com.orbiafly.api.modules.oneoffexpense.application.contract.OneOffExpenseServiceInterface;
import com.orbiafly.api.modules.oneoffexpense.application.factory.OneOffExpenseResultFactory;
import com.orbiafly.api.modules.oneoffexpense.application.model.MonthlyOneOffExpensesResult;
import com.orbiafly.api.modules.oneoffexpense.application.model.OneOffExpensePaymentResult;
import com.orbiafly.api.modules.oneoffexpense.application.model.OneOffExpenseResult;
import com.orbiafly.api.modules.oneoffexpense.domain.OneOffExpense;
import com.orbiafly.api.modules.oneoffexpense.infrastructure.persistence.OneOffExpenseRepositoryInterface;
import com.orbiafly.api.modules.oneoffexpense.web.dto.response.MonthlyOneOffExpensesResponseDTO;
import com.orbiafly.api.modules.oneoffexpense.web.dto.response.OneOffExpensePaymentResponseDTO;
import com.orbiafly.api.modules.oneoffexpense.web.dto.response.OneOffExpenseResponseDTO;
import com.orbiafly.api.modules.oneoffexpense.web.factory.MonthlyOneOffExpensesResponseDTOFactory;
import com.orbiafly.api.modules.oneoffexpense.web.factory.OneOffExpensePaymentResponseDTOFactory;
import com.orbiafly.api.modules.oneoffexpense.web.factory.OneOffExpenseResponseDTOFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
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
public class OneOffExpenseServiceImpl implements OneOffExpenseServiceInterface {
  private static final int MIN_YEAR = 2000;
  private static final int MAX_YEAR = 2200;

  private final OneOffExpenseRepositoryInterface expenseRepository;
  private final CategoryRepositoryInterface categoryRepository;
  private final FinancialAccountServiceInterface financialAccountService;
  private final OneOffExpenseResultFactory resultFactory;
  private final OneOffExpenseResponseDTOFactory responseFactory;
  private final MonthlyOneOffExpensesResponseDTOFactory monthlyResponseFactory;
  private final OneOffExpensePaymentResponseDTOFactory paymentResponseFactory;
  private final CurrentUserProvider currentUserProvider;
  private final Clock clock;

  public OneOffExpenseServiceImpl(
      OneOffExpenseRepositoryInterface expenseRepository,
      CategoryRepositoryInterface categoryRepository,
      FinancialAccountServiceInterface financialAccountService,
      OneOffExpenseResultFactory resultFactory,
      OneOffExpenseResponseDTOFactory responseFactory,
      MonthlyOneOffExpensesResponseDTOFactory monthlyResponseFactory,
      OneOffExpensePaymentResponseDTOFactory paymentResponseFactory,
      CurrentUserProvider currentUserProvider,
      Clock clock) {
    this.expenseRepository = expenseRepository;
    this.categoryRepository = categoryRepository;
    this.financialAccountService = financialAccountService;
    this.resultFactory = resultFactory;
    this.responseFactory = responseFactory;
    this.monthlyResponseFactory = monthlyResponseFactory;
    this.paymentResponseFactory = paymentResponseFactory;
    this.currentUserProvider = currentUserProvider;
    this.clock = clock;
  }

  @Override
  @Transactional(readOnly = true)
  public MonthlyOneOffExpensesResponseDTO getMonthlyOneOffExpenses(int year, int month) {
    validatePeriod(year, month);
    YearMonth competence = YearMonth.of(year, month);
    List<OneOffExpense> expenses = expenseRepository.findMonthly(
        currentUserProvider.currentUserId(), competence.atDay(1), competence.atEndOfMonth());
    Set<UUID> accountIds = expenses.stream().map(OneOffExpense::getFinancialAccountId)
        .filter(Objects::nonNull).collect(Collectors.toSet());
    Map<UUID, String> accountNames = financialAccountService.findFinancialAccountNames(accountIds);
    List<OneOffExpenseResult> items = expenses.stream()
        .map(expense -> resultFactory.createOneOffExpenseResult(expense, accountNames)).toList();
    return monthlyResponseFactory.createMonthlyOneOffExpensesResponseDTO(
        new MonthlyOneOffExpensesResult(year, month, summary(items), items));
  }

  @Override
  @Transactional
  public OneOffExpenseResponseDTO createOneOffExpense(
      String name, BigDecimal amount, LocalDate expenseDate, UUID categoryId) {
    OneOffExpense expense = new OneOffExpense(
        currentUserProvider.currentUserId(), name.trim(), amount, expenseDate,
        findCategory(categoryId), clock.instant());
    return responseFactory.createOneOffExpenseResponseDTO(
        resultFactory.createOneOffExpenseResult(expenseRepository.save(expense), Map.of()));
  }

  @Override
  @Transactional
  public OneOffExpenseResponseDTO updateOneOffExpense(
      UUID id, String name, BigDecimal amount, LocalDate expenseDate, UUID categoryId) {
    OneOffExpense expense = findExpenseForUpdate(id);
    if (expense.getStatus() == PaymentStatus.PAID) {
      throw new IllegalStateException("Volte o gasto para pendente antes de editá-lo.");
    }
    expense.updateOneOffExpense(name.trim(), amount, expenseDate, findCategory(categoryId), clock.instant());
    return responseFactory.createOneOffExpenseResponseDTO(
        resultFactory.createOneOffExpenseResult(expense, Map.of()));
  }

  @Override
  @Transactional
  public OneOffExpenseResponseDTO updateOneOffExpenseStatus(UUID id, PaymentStatus status) {
    OneOffExpense expense = findExpenseForUpdate(id);
    if (expense.getStatus() == status) {
      return toResponse(expense);
    }
    if (status == PaymentStatus.PAID) {
      if (financialAccountService.hasActiveFinancialAccounts()) {
        throw new IllegalStateException("Selecione uma conta para registrar o pagamento.");
      }
      expense.updateStatus(PaymentStatus.PAID, clock.instant());
      return toResponse(expense);
    }
    if (expense.getPaymentTransactionId() != null) {
      financialAccountService.reverseFinancialAccountDebit(
          expense.getPaymentTransactionId(), LocalDate.now(clock), "Estorno - " + expense.getName());
    }
    expense.updateStatus(PaymentStatus.PENDING, clock.instant());
    return toResponse(expense);
  }

  @Override
  @Transactional
  public OneOffExpensePaymentResponseDTO registerOneOffExpensePayment(
      UUID id, UUID financialAccountId, LocalDate paymentDate) {
    OneOffExpense expense = findExpenseForUpdate(id);
    if (expense.getStatus() == PaymentStatus.PAID) {
      if (Objects.equals(expense.getFinancialAccountId(), financialAccountId)
          && expense.getPaymentTransactionId() != null) {
        FinancialAccountResult account = financialAccountService.getFinancialAccountResult(financialAccountId);
        FinancialTransactionResult transaction = financialAccountService.getFinancialTransaction(
            expense.getPaymentTransactionId());
        return paymentResponseFactory.createOneOffExpensePaymentResponseDTO(
            paymentResult(expense, account, transaction));
      }
      throw new IllegalStateException("Este gasto já possui um pagamento registrado.");
    }
    AccountTransactionResult transaction = financialAccountService.debitFinancialAccountForReference(
        financialAccountId, expense.getAmount(), expense.getName(), paymentDate,
        FinancialReferenceType.ONE_OFF_EXPENSE, expense.getId());
    expense.registerPayment(transaction.financialAccount().id(), transaction.transaction().id(), clock.instant());
    return paymentResponseFactory.createOneOffExpensePaymentResponseDTO(
        paymentResult(expense, transaction.financialAccount(), transaction.transaction()));
  }

  @Override
  @Transactional
  public void deleteOneOffExpense(UUID id) {
    OneOffExpense expense = findExpenseForUpdate(id);
    if (expense.getStatus() == PaymentStatus.PAID) {
      throw new IllegalStateException("Volte o gasto para pendente antes de excluí-lo.");
    }
    expenseRepository.delete(expense);
  }

  private OneOffExpensePaymentResult paymentResult(
      OneOffExpense expense, FinancialAccountResult account, FinancialTransactionResult transaction) {
    OneOffExpenseResult view = resultFactory.createOneOffExpenseResult(
        expense, Map.of(account.id(), account.name()));
    return new OneOffExpensePaymentResult(
        view, transaction, account, account.currentBalance(), account.currentBalance().signum() < 0);
  }

  private OneOffExpenseResponseDTO toResponse(OneOffExpense expense) {
    Map<UUID, String> accountNames = expense.getFinancialAccountId() == null
        ? Map.of()
        : financialAccountService.findFinancialAccountNames(Set.of(expense.getFinancialAccountId()));
    return responseFactory.createOneOffExpenseResponseDTO(
        resultFactory.createOneOffExpenseResult(expense, accountNames));
  }

  private MonthlyExpenseSummary summary(List<OneOffExpenseResult> items) {
    BigDecimal paid = totalByStatus(items, PaymentStatus.PAID);
    BigDecimal pending = totalByStatus(items, PaymentStatus.PENDING);
    BigDecimal total = paid.add(pending);
    int paidCount = countByStatus(items, PaymentStatus.PAID);
    int pendingCount = countByStatus(items, PaymentStatus.PENDING);
    BigDecimal percentage = total.signum() == 0
        ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
        : paid.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP);
    return new MonthlyExpenseSummary(total, paid, pending, items.size(), paidCount, pendingCount, percentage);
  }

  private BigDecimal totalByStatus(List<OneOffExpenseResult> items, PaymentStatus status) {
    return items.stream().filter(item -> item.status() == status)
        .map(OneOffExpenseResult::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private int countByStatus(List<OneOffExpenseResult> items, PaymentStatus status) {
    return (int) items.stream().filter(item -> item.status() == status).count();
  }

  private OneOffExpense findExpenseForUpdate(UUID id) {
    return expenseRepository.findByIdAndOwnerUserIdForUpdate(id, currentUserProvider.currentUserId())
        .orElseThrow(() -> new ResourceNotFoundException("Gasto avulso não encontrado."));
  }

  private Category findCategory(UUID id) {
    return categoryRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada."));
  }

  private void validatePeriod(int year, int month) {
    if (year < MIN_YEAR || year > MAX_YEAR) {
      throw new IllegalArgumentException("O ano deve estar entre 2000 e 2200.");
    }
    YearMonth.of(year, month);
  }
}

package com.orbiafly.api.modules.financialaccount.application;

import com.orbiafly.api.modules.financialaccount.domain.FinancialAccount;
import com.orbiafly.api.modules.financialaccount.domain.FinancialAccountType;
import com.orbiafly.api.modules.financialaccount.domain.FinancialReferenceType;
import com.orbiafly.api.modules.financialaccount.domain.FinancialTransaction;
import com.orbiafly.api.modules.financialaccount.domain.FinancialTransactionType;
import com.orbiafly.api.modules.financialaccount.infrastructure.persistence.AccountBalanceProjection;
import com.orbiafly.api.modules.financialaccount.infrastructure.persistence.FinancialAccountRepository;
import com.orbiafly.api.modules.financialaccount.infrastructure.persistence.FinancialTransactionRepository;
import com.orbiafly.api.modules.fixedexpense.application.ResourceNotFoundException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FinancialAccountService {

  private final FinancialAccountRepository accountRepository;
  private final FinancialTransactionRepository transactionRepository;
  private final Clock clock;

  public FinancialAccountService(
      FinancialAccountRepository accountRepository,
      FinancialTransactionRepository transactionRepository,
      Clock clock) {
    this.accountRepository = accountRepository;
    this.transactionRepository = transactionRepository;
    this.clock = clock;
  }

  @Transactional(readOnly = true)
  public FinancialAccountsOverview list(boolean includeInactive) {
    List<FinancialAccount> accounts = includeInactive
        ? accountRepository.findAllByOrderByActiveDescNameAsc()
        : accountRepository.findAllByActiveTrueOrderByNameAsc();
    Map<UUID, BigDecimal> deltas = balanceDeltas();
    List<FinancialAccountView> views = accounts.stream()
        .map(account -> toView(account, currentBalance(account, deltas)))
        .toList();
    BigDecimal total = views.stream()
        .filter(FinancialAccountView::active)
        .map(FinancialAccountView::currentBalance)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    int activeCount = (int) views.stream().filter(FinancialAccountView::active).count();
    return new FinancialAccountsOverview(total, activeCount, views);
  }

  @Transactional(readOnly = true)
  public FinancialAccountView get(UUID id) {
    FinancialAccount account = findAccount(id);
    return toView(account, currentBalance(account));
  }

  @Transactional
  public FinancialAccountView create(
      String name,
      FinancialAccountType type,
      String institution,
      BigDecimal openingBalance) {
    Instant now = clock.instant();
    FinancialAccount account = new FinancialAccount(
        normalizeRequired(name),
        type,
        normalizeOptional(institution),
        openingBalance,
        now);
    return toView(accountRepository.save(account), openingBalance);
  }

  @Transactional
  public FinancialAccountView update(
      UUID id,
      String name,
      FinancialAccountType type,
      String institution) {
    FinancialAccount account = findAccountForUpdate(id);
    account.update(
        normalizeRequired(name),
        type,
        normalizeOptional(institution),
        clock.instant());
    return toView(account, currentBalance(account));
  }

  @Transactional
  public FinancialAccountView deactivate(UUID id) {
    FinancialAccount account = findAccountForUpdate(id);
    account.deactivate(clock.instant());
    return toView(account, currentBalance(account));
  }

  @Transactional(readOnly = true)
  public List<FinancialTransactionView> listTransactions(UUID accountId) {
    findAccount(accountId);
    return transactionRepository
        .findAllByFinancialAccountIdOrderByOccurredOnDescCreatedAtDesc(accountId)
        .stream()
        .map(this::toTransactionView)
        .toList();
  }

  @Transactional
  public AccountTransactionResult adjustBalance(
      UUID accountId,
      BigDecimal targetBalance,
      String description) {
    FinancialAccount account = findAccountForUpdate(accountId);
    BigDecimal current = currentBalance(account);
    BigDecimal difference = targetBalance.subtract(current);
    if (difference.signum() == 0) {
      throw new IllegalArgumentException("O saldo informado já é o saldo atual da conta.");
    }
    FinancialTransaction transaction = new FinancialTransaction(
        account,
        FinancialTransactionType.ADJUSTMENT,
        difference,
        normalizeDescription(description, "Ajuste manual de saldo"),
        LocalDate.now(clock),
        null,
        null,
        null,
        clock.instant());
    transactionRepository.saveAndFlush(transaction);
    return new AccountTransactionResult(
        toView(account, targetBalance),
        toTransactionView(transaction));
  }

  @Transactional
  public AccountTransactionResult debitForReference(
      UUID accountId,
      BigDecimal amount,
      String description,
      LocalDate occurredOn,
      FinancialReferenceType referenceType,
      UUID referenceId) {
    if (amount.signum() < 0) {
      throw new IllegalArgumentException("O valor do pagamento não pode ser negativo.");
    }
    FinancialAccount account = findAccountForUpdate(accountId);
    if (!account.isActive()) {
      throw new IllegalStateException("A conta selecionada está desativada.");
    }
    FinancialTransaction transaction = new FinancialTransaction(
        account,
        FinancialTransactionType.DEBIT,
        amount,
        normalizeRequired(description),
        occurredOn,
        referenceType,
        referenceId,
        null,
        clock.instant());
    transactionRepository.saveAndFlush(transaction);
    BigDecimal updatedBalance = currentBalance(account);
    return new AccountTransactionResult(
        toView(account, updatedBalance),
        toTransactionView(transaction));
  }

  @Transactional
  public AccountTransactionResult reverseDebit(
      UUID transactionId,
      LocalDate occurredOn,
      String description) {
    FinancialTransaction original = transactionRepository.findByIdForUpdate(transactionId)
        .orElseThrow(() -> new ResourceNotFoundException("Movimentação de pagamento não encontrada."));
    if (original.getType() != FinancialTransactionType.DEBIT) {
      throw new IllegalStateException("Somente movimentações de débito podem ser revertidas.");
    }
    if (transactionRepository.existsByReversalOfId(transactionId)) {
      throw new IllegalStateException("Este pagamento já possui uma reversão financeira.");
    }
    FinancialAccount account = findAccountForUpdate(original.getFinancialAccount().getId());
    FinancialTransaction reversal = new FinancialTransaction(
        account,
        FinancialTransactionType.CREDIT,
        original.getAmount(),
        normalizeRequired(description),
        occurredOn,
        original.getReferenceType(),
        original.getReferenceId(),
        original,
        clock.instant());
    transactionRepository.saveAndFlush(reversal);
    return new AccountTransactionResult(
        toView(account, currentBalance(account)),
        toTransactionView(reversal));
  }

  @Transactional(readOnly = true)
  public FinancialTransactionView getTransaction(UUID id) {
    return transactionRepository.findById(id)
        .map(this::toTransactionView)
        .orElseThrow(() -> new ResourceNotFoundException("Movimentação não encontrada."));
  }

  @Transactional(readOnly = true)
  public boolean hasActiveAccounts() {
    return accountRepository.existsByActiveTrue();
  }

  @Transactional(readOnly = true)
  public Map<UUID, String> findNames(Set<UUID> ids) {
    if (ids.isEmpty()) {
      return Map.of();
    }
    return accountRepository.findAllById(ids).stream()
        .collect(Collectors.toMap(FinancialAccount::getId, FinancialAccount::getName));
  }

  private FinancialAccount findAccount(UUID id) {
    return accountRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Conta financeira não encontrada."));
  }

  private FinancialAccount findAccountForUpdate(UUID id) {
    return accountRepository.findByIdForUpdate(id)
        .orElseThrow(() -> new ResourceNotFoundException("Conta financeira não encontrada."));
  }

  private BigDecimal currentBalance(FinancialAccount account) {
    return account.getOpeningBalance().add(transactionRepository.findBalanceDelta(account.getId()));
  }

  private BigDecimal currentBalance(
      FinancialAccount account,
      Map<UUID, BigDecimal> deltas) {
    return account.getOpeningBalance().add(deltas.getOrDefault(account.getId(), BigDecimal.ZERO));
  }

  private Map<UUID, BigDecimal> balanceDeltas() {
    Map<UUID, BigDecimal> deltas = new HashMap<>();
    transactionRepository.findBalanceDeltas().forEach(projection ->
        deltas.put(projection.getAccountId(), projection.getBalanceDelta()));
    return deltas;
  }

  private FinancialAccountView toView(FinancialAccount account, BigDecimal currentBalance) {
    return new FinancialAccountView(
        account.getId(),
        account.getName(),
        account.getType(),
        account.getInstitution(),
        account.getOpeningBalance(),
        currentBalance,
        account.isActive(),
        account.getCreatedAt(),
        account.getUpdatedAt());
  }

  private FinancialTransactionView toTransactionView(FinancialTransaction transaction) {
    return new FinancialTransactionView(
        transaction.getId(),
        transaction.getFinancialAccount().getId(),
        transaction.getType(),
        transaction.getAmount(),
        balanceEffect(transaction),
        transaction.getDescription(),
        transaction.getOccurredOn(),
        transaction.getReferenceType(),
        transaction.getReferenceId(),
        transaction.getReversalOf() == null ? null : transaction.getReversalOf().getId(),
        transaction.getCreatedAt());
  }

  private BigDecimal balanceEffect(FinancialTransaction transaction) {
    return switch (transaction.getType()) {
      case CREDIT -> transaction.getAmount();
      case DEBIT -> transaction.getAmount().negate();
      case ADJUSTMENT -> transaction.getAmount();
    };
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

  private String normalizeDescription(String value, String fallback) {
    return value == null || value.isBlank() ? fallback : value.trim();
  }
}

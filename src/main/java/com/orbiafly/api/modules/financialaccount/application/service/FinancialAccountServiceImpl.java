package com.orbiafly.api.modules.financialaccount.application.service;

import com.orbiafly.api.infrastructure.security.CurrentUserProvider;
import com.orbiafly.api.modules.financialaccount.application.contract.FinancialAccountServiceInterface;
import com.orbiafly.api.modules.financialaccount.application.factory.FinancialAccountResultFactory;
import com.orbiafly.api.modules.financialaccount.application.factory.FinancialTransactionResultFactory;
import com.orbiafly.api.modules.financialaccount.application.model.AccountTransactionResult;
import com.orbiafly.api.modules.financialaccount.application.model.FinancialAccountResult;
import com.orbiafly.api.modules.financialaccount.application.model.FinancialAccountsOverviewResult;
import com.orbiafly.api.modules.financialaccount.application.model.FinancialTransactionResult;
import com.orbiafly.api.modules.financialaccount.domain.FinancialAccount;
import com.orbiafly.api.modules.financialaccount.domain.FinancialAccountType;
import com.orbiafly.api.modules.financialaccount.domain.FinancialReferenceType;
import com.orbiafly.api.modules.financialaccount.domain.FinancialTransaction;
import com.orbiafly.api.modules.financialaccount.domain.FinancialTransactionType;
import com.orbiafly.api.modules.financialaccount.infrastructure.persistence.FinancialAccountRepositoryInterface;
import com.orbiafly.api.modules.financialaccount.infrastructure.persistence.FinancialTransactionRepositoryInterface;
import com.orbiafly.api.modules.financialaccount.web.dto.response.AccountTransactionResponseDTO;
import com.orbiafly.api.modules.financialaccount.web.dto.response.FinancialAccountResponseDTO;
import com.orbiafly.api.modules.financialaccount.web.dto.response.FinancialAccountsOverviewResponseDTO;
import com.orbiafly.api.modules.financialaccount.web.dto.response.FinancialTransactionResponseDTO;
import com.orbiafly.api.modules.financialaccount.web.factory.AccountTransactionResponseDTOFactory;
import com.orbiafly.api.modules.financialaccount.web.factory.FinancialAccountResponseDTOFactory;
import com.orbiafly.api.modules.financialaccount.web.factory.FinancialAccountsOverviewResponseDTOFactory;
import com.orbiafly.api.modules.financialaccount.web.factory.FinancialTransactionResponseDTOFactory;
import com.orbiafly.api.modules.fixedexpense.application.ResourceNotFoundException;
import com.orbiafly.api.modules.fixedexpense.domain.PaymentStatus;
import com.orbiafly.api.modules.fixedexpense.infrastructure.persistence.FixedExpenseOccurrenceRepositoryInterface;
import com.orbiafly.api.modules.oneoffexpense.infrastructure.persistence.OneOffExpenseRepositoryInterface;
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
public class FinancialAccountServiceImpl implements FinancialAccountServiceInterface {

  private final FinancialAccountRepositoryInterface accountRepository;
  private final FinancialTransactionRepositoryInterface transactionRepository;
  private final FinancialAccountResultFactory accountResultFactory;
  private final FinancialTransactionResultFactory transactionResultFactory;
  private final FinancialAccountResponseDTOFactory accountResponseDTOFactory;
  private final FinancialAccountsOverviewResponseDTOFactory overviewResponseDTOFactory;
  private final FinancialTransactionResponseDTOFactory transactionResponseDTOFactory;
  private final AccountTransactionResponseDTOFactory accountTransactionResponseDTOFactory;
  private final FixedExpenseOccurrenceRepositoryInterface occurrenceRepository;
  private final OneOffExpenseRepositoryInterface oneOffExpenseRepository;
  private final CurrentUserProvider currentUserProvider;
  private final Clock clock;

  public FinancialAccountServiceImpl(
      FinancialAccountRepositoryInterface accountRepository,
      FinancialTransactionRepositoryInterface transactionRepository,
      FinancialAccountResultFactory accountResultFactory,
      FinancialTransactionResultFactory transactionResultFactory,
      FinancialAccountResponseDTOFactory accountResponseDTOFactory,
      FinancialAccountsOverviewResponseDTOFactory overviewResponseDTOFactory,
      FinancialTransactionResponseDTOFactory transactionResponseDTOFactory,
      AccountTransactionResponseDTOFactory accountTransactionResponseDTOFactory,
      FixedExpenseOccurrenceRepositoryInterface occurrenceRepository,
      OneOffExpenseRepositoryInterface oneOffExpenseRepository,
      CurrentUserProvider currentUserProvider,
      Clock clock) {
    this.accountRepository = accountRepository;
    this.transactionRepository = transactionRepository;
    this.accountResultFactory = accountResultFactory;
    this.transactionResultFactory = transactionResultFactory;
    this.accountResponseDTOFactory = accountResponseDTOFactory;
    this.overviewResponseDTOFactory = overviewResponseDTOFactory;
    this.transactionResponseDTOFactory = transactionResponseDTOFactory;
    this.accountTransactionResponseDTOFactory = accountTransactionResponseDTOFactory;
    this.occurrenceRepository = occurrenceRepository;
    this.oneOffExpenseRepository = oneOffExpenseRepository;
    this.currentUserProvider = currentUserProvider;
    this.clock = clock;
  }

  @Override
  @Transactional(readOnly = true)
  public FinancialAccountsOverviewResponseDTO listFinancialAccounts(boolean includeInactive) {
    UUID ownerUserId = currentUserProvider.currentUserId();
    List<FinancialAccount> accounts = includeInactive
        ? accountRepository.findAllByOwnerUserIdOrderByActiveDescNameAsc(ownerUserId)
        : accountRepository.findAllByOwnerUserIdAndActiveTrueOrderByNameAsc(ownerUserId);
    Map<UUID, BigDecimal> deltas = balanceDeltas(ownerUserId);
    List<FinancialAccountResult> views = accounts.stream()
        .map(account -> accountResultFactory.createFinancialAccountResult(
            account,
            currentBalance(account, deltas)))
        .toList();
    BigDecimal total = views.stream()
        .filter(FinancialAccountResult::active)
        .map(FinancialAccountResult::currentBalance)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    int activeCount = (int) views.stream().filter(FinancialAccountResult::active).count();
    FinancialAccountsOverviewResult overview = new FinancialAccountsOverviewResult(
        total,
        activeCount,
        views);
    return overviewResponseDTOFactory.createFinancialAccountsOverviewResponseDTO(overview);
  }

  @Override
  @Transactional(readOnly = true)
  public FinancialAccountResponseDTO getFinancialAccount(UUID id) {
    return accountResponseDTOFactory.createFinancialAccountResponseDTO(
        getFinancialAccountResult(id));
  }

  @Override
  @Transactional(readOnly = true)
  public FinancialAccountResult getFinancialAccountResult(UUID id) {
    FinancialAccount account = findAccount(id);
    return accountResultFactory.createFinancialAccountResult(
        account,
        currentBalance(account));
  }

  @Override
  @Transactional
  public FinancialAccountResponseDTO createFinancialAccount(
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
        currentUserProvider.currentUserId(),
        now);
    FinancialAccountResult result = accountResultFactory.createFinancialAccountResult(
        accountRepository.save(account),
        openingBalance);
    return accountResponseDTOFactory.createFinancialAccountResponseDTO(result);
  }

  @Override
  @Transactional
  public FinancialAccountResponseDTO updateFinancialAccount(
      UUID id,
      String name,
      FinancialAccountType type,
      String institution) {
    FinancialAccount account = findAccountForUpdate(id);
    account.updateFinancialAccount(
        normalizeRequired(name),
        type,
        normalizeOptional(institution),
        clock.instant());
    FinancialAccountResult result = accountResultFactory.createFinancialAccountResult(
        account,
        currentBalance(account));
    return accountResponseDTOFactory.createFinancialAccountResponseDTO(result);
  }

  @Override
  @Transactional
  public FinancialAccountResponseDTO deactivateFinancialAccount(UUID id) {
    FinancialAccount account = findAccountForUpdate(id);
    account.deactivate(clock.instant());
    FinancialAccountResult result = accountResultFactory.createFinancialAccountResult(
        account,
        currentBalance(account));
    return accountResponseDTOFactory.createFinancialAccountResponseDTO(result);
  }

  @Override
  @Transactional
  public FinancialAccountResponseDTO reactivateFinancialAccount(UUID id) {
    FinancialAccount account = findAccountForUpdate(id);
    account.reactivateFinancialAccount(clock.instant());
    FinancialAccountResult result = accountResultFactory.createFinancialAccountResult(
        account,
        currentBalance(account));
    return accountResponseDTOFactory.createFinancialAccountResponseDTO(result);
  }

  @Override
  @Transactional
  public void deleteFinancialAccount(UUID id) {
    FinancialAccount account = findAccountForUpdate(id);
    if (account.isActive()) {
      throw new IllegalStateException("Desative a conta antes de excluí-la.");
    }
    occurrenceRepository.unlinkFinancialAccountPayments(
        account.getId(),
        account.getOwnerUserId(),
        PaymentStatus.PENDING,
        clock.instant());
    oneOffExpenseRepository.unlinkFinancialAccountPayments(
        account.getId(),
        account.getOwnerUserId(),
        clock.instant());
    transactionRepository.deleteAllByFinancialAccountIdAndOwnerUserId(
        account.getId(),
        account.getOwnerUserId());
    accountRepository.delete(account);
  }

  @Override
  @Transactional(readOnly = true)
  public List<FinancialTransactionResponseDTO> listFinancialAccountTransactions(UUID accountId) {
    findAccount(accountId);
    return transactionRepository
        .findAllByOwnerUserIdAndFinancialAccountIdOrderByOccurredOnDescCreatedAtDesc(
            currentUserProvider.currentUserId(),
            accountId)
        .stream()
        .map(transactionResultFactory::createFinancialTransactionResult)
        .map(transactionResponseDTOFactory::createFinancialTransactionResponseDTO)
        .toList();
  }

  @Override
  @Transactional
  public AccountTransactionResponseDTO adjustFinancialAccountBalance(
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
        account.getOwnerUserId(),
        null,
        clock.instant());
    transactionRepository.saveAndFlush(transaction);
    AccountTransactionResult result = new AccountTransactionResult(
        accountResultFactory.createFinancialAccountResult(account, targetBalance),
        transactionResultFactory.createFinancialTransactionResult(transaction));
    return accountTransactionResponseDTOFactory.createAccountTransactionResponseDTO(result);
  }

  @Override
  @Transactional
  public AccountTransactionResult debitFinancialAccountForReference(
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
        account.getOwnerUserId(),
        null,
        clock.instant());
    transactionRepository.saveAndFlush(transaction);
    BigDecimal updatedBalance = currentBalance(account);
    return new AccountTransactionResult(
        accountResultFactory.createFinancialAccountResult(account, updatedBalance),
        transactionResultFactory.createFinancialTransactionResult(transaction));
  }

  @Override
  @Transactional
  public AccountTransactionResult creditFinancialAccountForReference(
      UUID accountId,
      BigDecimal amount,
      String description,
      LocalDate occurredOn,
      FinancialReferenceType referenceType,
      UUID referenceId) {
    if (amount.signum() < 0) {
      throw new IllegalArgumentException("O valor do recebimento não pode ser negativo.");
    }
    FinancialAccount account = findAccountForUpdate(accountId);
    if (!account.isActive()) {
      throw new IllegalStateException("A conta selecionada está desativada.");
    }
    FinancialTransaction transaction = new FinancialTransaction(
        account,
        FinancialTransactionType.CREDIT,
        amount,
        normalizeRequired(description),
        occurredOn,
        referenceType,
        referenceId,
        account.getOwnerUserId(),
        null,
        clock.instant());
    transactionRepository.saveAndFlush(transaction);
    return new AccountTransactionResult(
        accountResultFactory.createFinancialAccountResult(account, currentBalance(account)),
        transactionResultFactory.createFinancialTransactionResult(transaction));
  }

  @Override
  @Transactional
  public AccountTransactionResult reverseFinancialAccountDebit(
      UUID transactionId,
      LocalDate occurredOn,
      String description) {
    FinancialTransaction original = transactionRepository.findByIdAndOwnerUserIdForUpdate(
            transactionId,
            currentUserProvider.currentUserId())
        .orElseThrow(() -> new ResourceNotFoundException("Movimentação de pagamento não encontrada."));
    return reverseDebitTransaction(original, occurredOn, description);
  }

  @Override
  @Transactional
  public void reverseFinancialAccountDebitForReference(
      FinancialReferenceType referenceType,
      UUID referenceId,
      LocalDate occurredOn,
      String description) {
    transactionRepository.findDebitByReferenceForUpdate(
            currentUserProvider.currentUserId(),
            referenceType,
            referenceId,
            FinancialTransactionType.DEBIT)
        .stream()
        .filter(transaction -> !transactionRepository.existsByReversalOfId(transaction.getId()))
        .findFirst()
        .ifPresent(transaction -> reverseDebitTransaction(transaction, occurredOn, description));
  }

  private AccountTransactionResult reverseDebitTransaction(
      FinancialTransaction original,
      LocalDate occurredOn,
      String description) {
    if (original.getType() != FinancialTransactionType.DEBIT) {
      throw new IllegalStateException("Somente movimentações de débito podem ser revertidas.");
    }
    if (transactionRepository.existsByReversalOfId(original.getId())) {
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
        account.getOwnerUserId(),
        original,
        clock.instant());
    transactionRepository.saveAndFlush(reversal);
    return new AccountTransactionResult(
        accountResultFactory.createFinancialAccountResult(account, currentBalance(account)),
        transactionResultFactory.createFinancialTransactionResult(reversal));
  }

  @Override
  @Transactional(readOnly = true)
  public FinancialTransactionResult getFinancialTransaction(UUID id) {
    return transactionRepository.findByIdAndOwnerUserIdForUpdate(
            id,
            currentUserProvider.currentUserId())
        .map(transactionResultFactory::createFinancialTransactionResult)
        .orElseThrow(() -> new ResourceNotFoundException("Movimentação não encontrada."));
  }

  @Override
  @Transactional(readOnly = true)
  public boolean hasActiveFinancialAccounts() {
    return accountRepository.existsByOwnerUserIdAndActiveTrue(currentUserProvider.currentUserId());
  }

  @Override
  @Transactional(readOnly = true)
  public Map<UUID, String> findFinancialAccountNames(Set<UUID> ids) {
    if (ids.isEmpty()) {
      return Map.of();
    }
    UUID ownerUserId = currentUserProvider.currentUserId();
    return accountRepository.findAllById(ids).stream()
        .filter(account -> account.getOwnerUserId().equals(ownerUserId))
        .collect(Collectors.toMap(FinancialAccount::getId, FinancialAccount::getName));
  }

  private FinancialAccount findAccount(UUID id) {
    return accountRepository.findByIdAndOwnerUserId(id, currentUserProvider.currentUserId())
        .orElseThrow(() -> new ResourceNotFoundException("Conta financeira não encontrada."));
  }

  private FinancialAccount findAccountForUpdate(UUID id) {
    return accountRepository.findByIdAndOwnerUserIdForUpdate(id, currentUserProvider.currentUserId())
        .orElseThrow(() -> new ResourceNotFoundException("Conta financeira não encontrada."));
  }

  private BigDecimal currentBalance(FinancialAccount account) {
    return account.getOpeningBalance().add(transactionRepository.findBalanceDelta(
        account.getId(),
        account.getOwnerUserId()));
  }

  private BigDecimal currentBalance(
      FinancialAccount account,
      Map<UUID, BigDecimal> deltas) {
    return account.getOpeningBalance().add(deltas.getOrDefault(account.getId(), BigDecimal.ZERO));
  }

  private Map<UUID, BigDecimal> balanceDeltas(UUID ownerUserId) {
    Map<UUID, BigDecimal> deltas = new HashMap<>();
    transactionRepository.findBalanceDeltas(ownerUserId).forEach(projection ->
        deltas.put(projection.getAccountId(), projection.getBalanceDelta()));
    return deltas;
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

package com.orbiafly.api.modules.accounttransfer.application.service;

import com.orbiafly.api.infrastructure.security.CurrentUserProvider;
import com.orbiafly.api.modules.accounttransfer.application.contract.AccountTransferServiceInterface;
import com.orbiafly.api.modules.accounttransfer.application.factory.AccountTransferResultFactory;
import com.orbiafly.api.modules.accounttransfer.application.model.AccountTransferCreationResult;
import com.orbiafly.api.modules.accounttransfer.application.model.AccountTransferOverviewResult;
import com.orbiafly.api.modules.accounttransfer.application.model.AccountTransferResult;
import com.orbiafly.api.modules.accounttransfer.domain.AccountTransfer;
import com.orbiafly.api.modules.accounttransfer.infrastructure.persistence.AccountTransferRepositoryInterface;
import com.orbiafly.api.modules.accounttransfer.web.dto.response.AccountTransferCreationResponseDTO;
import com.orbiafly.api.modules.accounttransfer.web.dto.response.AccountTransferOverviewResponseDTO;
import com.orbiafly.api.modules.accounttransfer.web.dto.response.AccountTransferResponseDTO;
import com.orbiafly.api.modules.accounttransfer.web.factory.AccountTransferCreationResponseDTOFactory;
import com.orbiafly.api.modules.accounttransfer.web.factory.AccountTransferOverviewResponseDTOFactory;
import com.orbiafly.api.modules.accounttransfer.web.factory.AccountTransferResponseDTOFactory;
import com.orbiafly.api.modules.financialaccount.application.factory.FinancialAccountResultFactory;
import com.orbiafly.api.modules.financialaccount.application.factory.FinancialTransactionResultFactory;
import com.orbiafly.api.modules.financialaccount.application.model.AccountTransactionResult;
import com.orbiafly.api.modules.financialaccount.domain.FinancialAccount;
import com.orbiafly.api.modules.financialaccount.domain.FinancialReferenceType;
import com.orbiafly.api.modules.financialaccount.domain.FinancialTransaction;
import com.orbiafly.api.modules.financialaccount.domain.FinancialTransactionType;
import com.orbiafly.api.modules.financialaccount.infrastructure.persistence.FinancialAccountRepositoryInterface;
import com.orbiafly.api.modules.financialaccount.infrastructure.persistence.FinancialTransactionRepositoryInterface;
import com.orbiafly.api.modules.fixedexpense.application.ResourceNotFoundException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountTransferServiceImpl implements AccountTransferServiceInterface {

  private final AccountTransferRepositoryInterface transferRepository;
  private final FinancialAccountRepositoryInterface accountRepository;
  private final FinancialTransactionRepositoryInterface transactionRepository;
  private final AccountTransferResultFactory transferResultFactory;
  private final FinancialAccountResultFactory accountResultFactory;
  private final FinancialTransactionResultFactory transactionResultFactory;
  private final AccountTransferResponseDTOFactory transferResponseDTOFactory;
  private final AccountTransferOverviewResponseDTOFactory overviewResponseDTOFactory;
  private final AccountTransferCreationResponseDTOFactory creationResponseDTOFactory;
  private final CurrentUserProvider currentUserProvider;
  private final Clock clock;

  public AccountTransferServiceImpl(
      AccountTransferRepositoryInterface transferRepository,
      FinancialAccountRepositoryInterface accountRepository,
      FinancialTransactionRepositoryInterface transactionRepository,
      AccountTransferResultFactory transferResultFactory,
      FinancialAccountResultFactory accountResultFactory,
      FinancialTransactionResultFactory transactionResultFactory,
      AccountTransferResponseDTOFactory transferResponseDTOFactory,
      AccountTransferOverviewResponseDTOFactory overviewResponseDTOFactory,
      AccountTransferCreationResponseDTOFactory creationResponseDTOFactory,
      CurrentUserProvider currentUserProvider,
      Clock clock) {
    this.transferRepository = transferRepository;
    this.accountRepository = accountRepository;
    this.transactionRepository = transactionRepository;
    this.transferResultFactory = transferResultFactory;
    this.accountResultFactory = accountResultFactory;
    this.transactionResultFactory = transactionResultFactory;
    this.transferResponseDTOFactory = transferResponseDTOFactory;
    this.overviewResponseDTOFactory = overviewResponseDTOFactory;
    this.creationResponseDTOFactory = creationResponseDTOFactory;
    this.currentUserProvider = currentUserProvider;
    this.clock = clock;
  }

  @Override
  @Transactional(readOnly = true)
  public AccountTransferOverviewResponseDTO listAccountTransfers() {
    UUID ownerUserId = currentUserProvider.currentUserId();
    List<AccountTransferResult> items = transferRepository
        .findAllByOwnerUserIdOrderByTransferDateDescCreatedAtDesc(ownerUserId)
        .stream()
        .map(transferResultFactory::createAccountTransferResult)
        .toList();
    BigDecimal total = items.stream()
        .map(AccountTransferResult::amount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    return overviewResponseDTOFactory.createAccountTransferOverviewResponseDTO(
        new AccountTransferOverviewResult(total, items.size(), items));
  }

  @Override
  @Transactional(readOnly = true)
  public AccountTransferResponseDTO getAccountTransfer(UUID id) {
    return transferRepository.findByIdAndOwnerUserId(id, currentUserProvider.currentUserId())
        .map(transferResultFactory::createAccountTransferResult)
        .map(transferResponseDTOFactory::createAccountTransferResponseDTO)
        .orElseThrow(() -> new ResourceNotFoundException("Transferência não encontrada."));
  }

  @Override
  @Transactional
  public AccountTransferCreationResponseDTO createAccountTransfer(
      UUID sourceAccountId,
      UUID destinationAccountId,
      BigDecimal amount,
      LocalDate transferDate,
      String description) {
    validate(sourceAccountId, destinationAccountId, amount, transferDate);
    UUID ownerUserId = currentUserProvider.currentUserId();
    AccountTransferAccounts accounts = findAccountsForUpdate(sourceAccountId, destinationAccountId, ownerUserId);
    FinancialAccount source = accounts.source();
    FinancialAccount destination = accounts.destination();
    if (!source.isActive() || !destination.isActive()) {
      throw new IllegalStateException("As contas da transferência precisam estar ativas.");
    }
    Instant now = clock.instant();
    AccountTransfer transfer = new AccountTransfer(
        source,
        destination,
        amount,
        transferDate,
        normalizeDescription(description, destination.getName()),
        ownerUserId,
        now);
    transferRepository.save(transfer);

    FinancialTransaction sourceTransaction = new FinancialTransaction(
        source,
        FinancialTransactionType.TRANSFER_OUT,
        amount,
        "Transferência para " + destination.getName(),
        transferDate,
        FinancialReferenceType.ACCOUNT_TRANSFER,
        transfer.getId(),
        ownerUserId,
        null,
        now);
    FinancialTransaction destinationTransaction = new FinancialTransaction(
        destination,
        FinancialTransactionType.TRANSFER_IN,
        amount,
        "Transferência de " + source.getName(),
        transferDate,
        FinancialReferenceType.ACCOUNT_TRANSFER,
        transfer.getId(),
        ownerUserId,
        null,
        now);
    transactionRepository.save(sourceTransaction);
    transactionRepository.save(destinationTransaction);
    transfer.attachTransactions(sourceTransaction.getId(), destinationTransaction.getId(), now);
    transactionRepository.flush();

    AccountTransactionResult sourceResult = new AccountTransactionResult(
        accountResultFactory.createFinancialAccountResult(source, currentBalance(source)),
        transactionResultFactory.createFinancialTransactionResult(sourceTransaction));
    AccountTransactionResult destinationResult = new AccountTransactionResult(
        accountResultFactory.createFinancialAccountResult(destination, currentBalance(destination)),
        transactionResultFactory.createFinancialTransactionResult(destinationTransaction));
    AccountTransferCreationResult result = new AccountTransferCreationResult(
        transferResultFactory.createAccountTransferResult(transfer),
        sourceResult,
        destinationResult);
    return creationResponseDTOFactory.createAccountTransferCreationResponseDTO(result);
  }

  private FinancialAccount findAccountForUpdate(UUID accountId, UUID ownerUserId) {
    return accountRepository.findByIdAndOwnerUserIdForUpdate(accountId, ownerUserId)
        .orElseThrow(() -> new ResourceNotFoundException("Conta financeira não encontrada."));
  }

  private AccountTransferAccounts findAccountsForUpdate(
      UUID sourceAccountId,
      UUID destinationAccountId,
      UUID ownerUserId) {
    if (sourceAccountId.compareTo(destinationAccountId) < 0) {
      FinancialAccount source = findAccountForUpdate(sourceAccountId, ownerUserId);
      FinancialAccount destination = findAccountForUpdate(destinationAccountId, ownerUserId);
      return new AccountTransferAccounts(source, destination);
    }
    FinancialAccount destination = findAccountForUpdate(destinationAccountId, ownerUserId);
    FinancialAccount source = findAccountForUpdate(sourceAccountId, ownerUserId);
    return new AccountTransferAccounts(source, destination);
  }

  private BigDecimal currentBalance(FinancialAccount account) {
    return account.getOpeningBalance().add(transactionRepository.findBalanceDelta(
        account.getId(),
        account.getOwnerUserId()));
  }

  private void validate(
      UUID sourceAccountId,
      UUID destinationAccountId,
      BigDecimal amount,
      LocalDate transferDate) {
    if (sourceAccountId == null || destinationAccountId == null) {
      throw new IllegalArgumentException("Informe as contas da transferência.");
    }
    if (sourceAccountId.equals(destinationAccountId)) {
      throw new IllegalArgumentException("A conta de origem deve ser diferente da conta de destino.");
    }
    if (amount == null || amount.signum() <= 0) {
      throw new IllegalArgumentException("Informe um valor maior que zero.");
    }
    if (transferDate == null) {
      throw new IllegalArgumentException("Informe a data da transferência.");
    }
  }

  private String normalizeDescription(String value, String destinationName) {
    if (value == null || value.isBlank()) {
      return "Transferência para " + destinationName;
    }
    return value.trim();
  }

  private record AccountTransferAccounts(FinancialAccount source, FinancialAccount destination) {
  }
}

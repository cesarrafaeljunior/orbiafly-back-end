package com.orbiafly.api.modules.expectedincome.application.service;

import com.orbiafly.api.infrastructure.security.CurrentUserProvider;
import com.orbiafly.api.modules.expectedincome.application.contract.ExpectedIncomeServiceInterface;
import com.orbiafly.api.modules.expectedincome.application.factory.ExpectedIncomeResultFactory;
import com.orbiafly.api.modules.expectedincome.application.model.ExpectedIncomeOverviewResult;
import com.orbiafly.api.modules.expectedincome.application.model.ExpectedIncomeReceiptResult;
import com.orbiafly.api.modules.expectedincome.application.model.ExpectedIncomeResult;
import com.orbiafly.api.modules.expectedincome.domain.ExpectedIncome;
import com.orbiafly.api.modules.expectedincome.domain.ExpectedIncomeStatus;
import com.orbiafly.api.modules.expectedincome.infrastructure.persistence.ExpectedIncomeRepositoryInterface;
import com.orbiafly.api.modules.expectedincome.web.dto.response.ExpectedIncomeOverviewResponseDTO;
import com.orbiafly.api.modules.expectedincome.web.dto.response.ExpectedIncomeReceiptResponseDTO;
import com.orbiafly.api.modules.expectedincome.web.dto.response.ExpectedIncomeResponseDTO;
import com.orbiafly.api.modules.expectedincome.web.factory.ExpectedIncomeOverviewResponseDTOFactory;
import com.orbiafly.api.modules.expectedincome.web.factory.ExpectedIncomeReceiptResponseDTOFactory;
import com.orbiafly.api.modules.expectedincome.web.factory.ExpectedIncomeResponseDTOFactory;
import com.orbiafly.api.modules.financialaccount.application.contract.FinancialAccountServiceInterface;
import com.orbiafly.api.modules.financialaccount.application.model.AccountTransactionResult;
import com.orbiafly.api.modules.financialaccount.domain.FinancialAccount;
import com.orbiafly.api.modules.financialaccount.domain.FinancialReferenceType;
import com.orbiafly.api.modules.financialaccount.infrastructure.persistence.FinancialAccountRepositoryInterface;
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
public class ExpectedIncomeServiceImpl implements ExpectedIncomeServiceInterface {

  private final ExpectedIncomeRepositoryInterface expectedIncomeRepository;
  private final FinancialAccountRepositoryInterface accountRepository;
  private final FinancialAccountServiceInterface financialAccountService;
  private final ExpectedIncomeResultFactory resultFactory;
  private final ExpectedIncomeResponseDTOFactory responseDTOFactory;
  private final ExpectedIncomeOverviewResponseDTOFactory overviewResponseDTOFactory;
  private final ExpectedIncomeReceiptResponseDTOFactory receiptResponseDTOFactory;
  private final CurrentUserProvider currentUserProvider;
  private final Clock clock;

  public ExpectedIncomeServiceImpl(
      ExpectedIncomeRepositoryInterface expectedIncomeRepository,
      FinancialAccountRepositoryInterface accountRepository,
      FinancialAccountServiceInterface financialAccountService,
      ExpectedIncomeResultFactory resultFactory,
      ExpectedIncomeResponseDTOFactory responseDTOFactory,
      ExpectedIncomeOverviewResponseDTOFactory overviewResponseDTOFactory,
      ExpectedIncomeReceiptResponseDTOFactory receiptResponseDTOFactory,
      CurrentUserProvider currentUserProvider,
      Clock clock) {
    this.expectedIncomeRepository = expectedIncomeRepository;
    this.accountRepository = accountRepository;
    this.financialAccountService = financialAccountService;
    this.resultFactory = resultFactory;
    this.responseDTOFactory = responseDTOFactory;
    this.overviewResponseDTOFactory = overviewResponseDTOFactory;
    this.receiptResponseDTOFactory = receiptResponseDTOFactory;
    this.currentUserProvider = currentUserProvider;
    this.clock = clock;
  }

  @Override
  @Transactional(readOnly = true)
  public ExpectedIncomeOverviewResponseDTO listExpectedIncomes() {
    UUID ownerUserId = currentUserProvider.currentUserId();
    List<ExpectedIncomeResult> items = expectedIncomeRepository
        .findAllByOwnerUserIdAndDeletedAtIsNullOrderByExpectedDateAscCreatedAtDesc(ownerUserId)
        .stream()
        .map(resultFactory::createExpectedIncomeResult)
        .toList();
    ExpectedIncomeOverviewResult overview = new ExpectedIncomeOverviewResult(
        totalByStatus(items, ExpectedIncomeStatus.PENDING),
        totalByStatus(items, ExpectedIncomeStatus.RECEIVED),
        totalByStatus(items, ExpectedIncomeStatus.CANCELED),
        countByStatus(items, ExpectedIncomeStatus.PENDING),
        countByStatus(items, ExpectedIncomeStatus.RECEIVED),
        countByStatus(items, ExpectedIncomeStatus.CANCELED),
        items);
    return overviewResponseDTOFactory.createExpectedIncomeOverviewResponseDTO(overview);
  }

  @Override
  @Transactional(readOnly = true)
  public ExpectedIncomeResponseDTO getExpectedIncome(UUID id) {
    return responseDTOFactory.createExpectedIncomeResponseDTO(
        resultFactory.createExpectedIncomeResult(findIncome(id)));
  }

  @Override
  @Transactional
  public ExpectedIncomeResponseDTO createExpectedIncome(
      String description,
      BigDecimal amount,
      LocalDate expectedDate,
      UUID financialAccountId,
      String notes) {
    validateAmount(amount);
    Instant now = clock.instant();
    ExpectedIncome income = new ExpectedIncome(
        normalizeRequired(description),
        amount,
        expectedDate,
        findActiveAccount(financialAccountId),
        normalizeOptional(notes),
        currentUserProvider.currentUserId(),
        now);
    return responseDTOFactory.createExpectedIncomeResponseDTO(
        resultFactory.createExpectedIncomeResult(expectedIncomeRepository.save(income)));
  }

  @Override
  @Transactional
  public ExpectedIncomeResponseDTO updateExpectedIncome(
      UUID id,
      String description,
      BigDecimal amount,
      LocalDate expectedDate,
      UUID financialAccountId,
      String notes) {
    validateAmount(amount);
    ExpectedIncome income = findIncomeForUpdate(id);
    income.update(
        normalizeRequired(description),
        amount,
        expectedDate,
        findActiveAccount(financialAccountId),
        normalizeOptional(notes),
        clock.instant());
    return responseDTOFactory.createExpectedIncomeResponseDTO(
        resultFactory.createExpectedIncomeResult(income));
  }

  @Override
  @Transactional
  public ExpectedIncomeReceiptResponseDTO receiveExpectedIncome(
      UUID id,
      UUID financialAccountId,
      LocalDate receivedDate) {
    ExpectedIncome income = findIncomeForUpdate(id);
    if (income.getStatus() == ExpectedIncomeStatus.RECEIVED) {
      throw new IllegalStateException("Este recebimento já foi efetivado.");
    }
    if (income.getStatus() == ExpectedIncomeStatus.CANCELED) {
      throw new IllegalStateException("Recebimentos cancelados não podem ser efetivados.");
    }
    UUID accountId = financialAccountId != null
        ? financialAccountId
        : income.getFinancialAccount() == null ? null : income.getFinancialAccount().getId();
    if (accountId == null) {
      throw new IllegalStateException("Selecione uma conta para efetivar o recebimento.");
    }
    AccountTransactionResult transaction = financialAccountService.creditFinancialAccountForReference(
        accountId,
        income.getAmount(),
        "Recebimento - " + income.getDescription(),
        receivedDate == null ? LocalDate.now(clock) : receivedDate,
        FinancialReferenceType.EXPECTED_INCOME,
        income.getId());
    income.markReceived(transaction.transaction().id(), clock.instant());
    ExpectedIncomeReceiptResult result = new ExpectedIncomeReceiptResult(
        resultFactory.createExpectedIncomeResult(income),
        transaction);
    return receiptResponseDTOFactory.createExpectedIncomeReceiptResponseDTO(result);
  }

  @Override
  @Transactional
  public ExpectedIncomeResponseDTO cancelExpectedIncome(UUID id) {
    ExpectedIncome income = findIncomeForUpdate(id);
    income.cancel(clock.instant());
    return responseDTOFactory.createExpectedIncomeResponseDTO(
        resultFactory.createExpectedIncomeResult(income));
  }

  private ExpectedIncome findIncome(UUID id) {
    return expectedIncomeRepository.findByIdAndOwnerUserIdAndDeletedAtIsNull(
            id,
            currentUserProvider.currentUserId())
        .orElseThrow(() -> new ResourceNotFoundException("Recebimento não encontrado."));
  }

  private ExpectedIncome findIncomeForUpdate(UUID id) {
    return expectedIncomeRepository.findByIdAndOwnerUserIdForUpdate(
            id,
            currentUserProvider.currentUserId())
        .orElseThrow(() -> new ResourceNotFoundException("Recebimento não encontrado."));
  }

  private FinancialAccount findActiveAccount(UUID id) {
    if (id == null) {
      return null;
    }
    FinancialAccount account = accountRepository.findByIdAndOwnerUserId(
            id,
            currentUserProvider.currentUserId())
        .orElseThrow(() -> new ResourceNotFoundException("Conta financeira não encontrada."));
    if (!account.isActive()) {
      throw new IllegalStateException("A conta selecionada está desativada.");
    }
    return account;
  }

  private void validateAmount(BigDecimal amount) {
    if (amount == null || amount.signum() <= 0) {
      throw new IllegalArgumentException("Informe um valor maior que zero.");
    }
  }

  private BigDecimal totalByStatus(List<ExpectedIncomeResult> items, ExpectedIncomeStatus status) {
    return items.stream()
        .filter(item -> item.status() == status)
        .map(ExpectedIncomeResult::amount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private int countByStatus(List<ExpectedIncomeResult> items, ExpectedIncomeStatus status) {
    return (int) items.stream().filter(item -> item.status() == status).count();
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

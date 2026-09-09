package com.orbiafly.api.modules.creditcard.application.service;

import com.orbiafly.api.infrastructure.security.CurrentUserProvider;
import com.orbiafly.api.modules.creditcard.application.contract.CreditCardPurchaseServiceInterface;
import com.orbiafly.api.modules.creditcard.application.factory.CreditCardPurchaseResultFactory;
import com.orbiafly.api.modules.creditcard.application.model.CreditCardMonthlyPurchaseInstallmentResult;
import com.orbiafly.api.modules.creditcard.application.model.CreditCardMonthlyPurchaseSummaryResult;
import com.orbiafly.api.modules.creditcard.application.model.CreditCardMonthlyPurchasesResult;
import com.orbiafly.api.modules.creditcard.application.model.CreditCardPurchaseOverviewResult;
import com.orbiafly.api.modules.creditcard.application.model.CreditCardPurchaseResult;
import com.orbiafly.api.modules.creditcard.domain.CreditCard;
import com.orbiafly.api.modules.creditcard.domain.CreditCardInstallment;
import com.orbiafly.api.modules.creditcard.domain.CreditCardInstallmentStatus;
import com.orbiafly.api.modules.creditcard.domain.CreditCardPurchase;
import com.orbiafly.api.modules.creditcard.domain.CreditCardPurchaseStatus;
import com.orbiafly.api.modules.creditcard.domain.CreditCardPurchaseType;
import com.orbiafly.api.modules.creditcard.infrastructure.persistence.CreditCardInstallmentRepositoryInterface;
import com.orbiafly.api.modules.creditcard.infrastructure.persistence.CreditCardPurchaseRepositoryInterface;
import com.orbiafly.api.modules.creditcard.infrastructure.persistence.CreditCardRepositoryInterface;
import com.orbiafly.api.modules.creditcard.web.dto.response.CreditCardMonthlyPurchaseInstallmentResponseDTO;
import com.orbiafly.api.modules.creditcard.web.dto.response.CreditCardMonthlyPurchasesResponseDTO;
import com.orbiafly.api.modules.creditcard.web.dto.response.CreditCardPurchaseOverviewResponseDTO;
import com.orbiafly.api.modules.creditcard.web.dto.response.CreditCardPurchaseResponseDTO;
import com.orbiafly.api.modules.creditcard.web.factory.CreditCardMonthlyPurchasesResponseDTOFactory;
import com.orbiafly.api.modules.creditcard.web.factory.CreditCardPurchaseOverviewResponseDTOFactory;
import com.orbiafly.api.modules.creditcard.web.factory.CreditCardPurchaseResponseDTOFactory;
import com.orbiafly.api.modules.financialaccount.application.contract.FinancialAccountServiceInterface;
import com.orbiafly.api.modules.financialaccount.domain.FinancialReferenceType;
import com.orbiafly.api.modules.fixedexpense.application.ResourceNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreditCardPurchaseServiceImpl implements CreditCardPurchaseServiceInterface {

  private static final int MIN_YEAR = 2000;
  private static final int MAX_YEAR = 2200;

  private final CreditCardRepositoryInterface cardRepository;
  private final CreditCardPurchaseRepositoryInterface purchaseRepository;
  private final CreditCardInstallmentRepositoryInterface installmentRepository;
  private final CreditCardPurchaseResultFactory purchaseResultFactory;
  private final CreditCardPurchaseResponseDTOFactory purchaseResponseDTOFactory;
  private final CreditCardPurchaseOverviewResponseDTOFactory overviewResponseDTOFactory;
  private final CreditCardMonthlyPurchasesResponseDTOFactory monthlyPurchasesResponseDTOFactory;
  private final FinancialAccountServiceInterface financialAccountService;
  private final CurrentUserProvider currentUserProvider;
  private final Clock clock;

  public CreditCardPurchaseServiceImpl(
      CreditCardRepositoryInterface cardRepository,
      CreditCardPurchaseRepositoryInterface purchaseRepository,
      CreditCardInstallmentRepositoryInterface installmentRepository,
      CreditCardPurchaseResultFactory purchaseResultFactory,
      CreditCardPurchaseResponseDTOFactory purchaseResponseDTOFactory,
      CreditCardPurchaseOverviewResponseDTOFactory overviewResponseDTOFactory,
      CreditCardMonthlyPurchasesResponseDTOFactory monthlyPurchasesResponseDTOFactory,
      FinancialAccountServiceInterface financialAccountService,
      CurrentUserProvider currentUserProvider,
      Clock clock) {
    this.cardRepository = cardRepository;
    this.purchaseRepository = purchaseRepository;
    this.installmentRepository = installmentRepository;
    this.purchaseResultFactory = purchaseResultFactory;
    this.purchaseResponseDTOFactory = purchaseResponseDTOFactory;
    this.overviewResponseDTOFactory = overviewResponseDTOFactory;
    this.monthlyPurchasesResponseDTOFactory = monthlyPurchasesResponseDTOFactory;
    this.financialAccountService = financialAccountService;
    this.currentUserProvider = currentUserProvider;
    this.clock = clock;
  }

  @Override
  @Transactional(readOnly = true)
  public CreditCardPurchaseOverviewResponseDTO listCreditCardPurchases() {
    YearMonth currentCompetence = YearMonth.now(clock);
    List<CreditCardPurchaseResult> purchases = purchaseRepository
        .findAllByOwnerUserIdAndDeletedAtIsNullOrderByPurchaseDateDescCreatedAtDesc(
            currentUserProvider.currentUserId())
        .stream()
        .map(purchase -> purchaseResultFactory.createCreditCardPurchaseResult(
            purchase,
            currentCompetence))
        .toList();
    BigDecimal totalCommitted = purchases.stream()
        .flatMap(purchase -> purchase.installments().stream())
        .filter(installment -> installment.status() == CreditCardInstallmentStatus.PENDING)
        .map(installment -> installment.amount())
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    int activePurchaseCount = (int) purchases.stream()
        .filter(purchase -> purchase.status() == CreditCardPurchaseStatus.EM_ANDAMENTO)
        .count();
    int futureInstallmentCount = purchases.stream()
        .mapToInt(CreditCardPurchaseResult::futureInstallmentCount)
        .sum();
    BigDecimal currentMonthAmount = purchases.stream()
        .flatMap(purchase -> purchase.installments().stream())
        .filter(installment -> installment.competenceYear() == currentCompetence.getYear()
            && installment.competenceMonth() == currentCompetence.getMonthValue())
        .map(installment -> installment.amount())
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    return overviewResponseDTOFactory.createCreditCardPurchaseOverviewResponseDTO(
        new CreditCardPurchaseOverviewResult(
            totalCommitted,
            activePurchaseCount,
            futureInstallmentCount,
            currentMonthAmount,
            purchases));
  }

  @Override
  @Transactional(readOnly = true)
  public CreditCardMonthlyPurchasesResponseDTO getMonthlyCreditCardPurchases(int year, int month) {
    validatePeriod(year, month);
    YearMonth competence = YearMonth.of(year, month);
    List<CreditCardMonthlyPurchaseInstallmentResult> items = purchaseRepository
        .findAllByOwnerUserIdAndDeletedAtIsNullOrderByPurchaseDateDescCreatedAtDesc(
            currentUserProvider.currentUserId())
        .stream()
        .flatMap(purchase -> purchase.getInstallments().stream()
            .filter(installment -> installment.getCompetenceYear() == year
                && installment.getCompetenceMonth() == month)
            .map(installment -> createMonthlyPurchaseInstallmentResult(purchase, installment, competence)))
        .sorted(Comparator
            .comparing(CreditCardMonthlyPurchaseInstallmentResult::dueDate)
            .thenComparing(CreditCardMonthlyPurchaseInstallmentResult::description)
            .thenComparingInt(CreditCardMonthlyPurchaseInstallmentResult::installmentNumber))
        .toList();
    CreditCardMonthlyPurchasesResult result = new CreditCardMonthlyPurchasesResult(
        year,
        month,
        createSummary(items),
        items);
    return monthlyPurchasesResponseDTOFactory.createCreditCardMonthlyPurchasesResponseDTO(result);
  }

  @Override
  @Transactional(readOnly = true)
  public CreditCardPurchaseResponseDTO getCreditCardPurchase(UUID id) {
    return purchaseRepository.findByIdAndOwnerUserIdAndDeletedAtIsNull(
            id,
            currentUserProvider.currentUserId())
        .map(purchase -> purchaseResultFactory.createCreditCardPurchaseResult(
            purchase,
            YearMonth.now(clock)))
        .map(purchaseResponseDTOFactory::createCreditCardPurchaseResponseDTO)
        .orElseThrow(() -> new ResourceNotFoundException("Compra do cartão não encontrada."));
  }

  @Override
  @Transactional
  public CreditCardPurchaseResponseDTO createCreditCardPurchase(
      String description,
      UUID creditCardId,
      BigDecimal totalAmount,
      LocalDate purchaseDate,
      CreditCardPurchaseType purchaseType,
      UUID financialAccountId,
      Integer installmentCount) {
    CreditCardPurchaseType normalizedType = normalizePurchaseType(purchaseType);
    int normalizedInstallmentCount = normalizeInstallmentCount(normalizedType, installmentCount);
    validatePurchase(totalAmount, normalizedInstallmentCount);
    UUID ownerUserId = currentUserProvider.currentUserId();
    CreditCard card = cardRepository.findByIdAndOwnerUserIdForUpdate(creditCardId, ownerUserId)
        .orElseThrow(() -> new ResourceNotFoundException("Cartão de crédito não encontrado."));
    if (!card.isActive()) {
      throw new IllegalStateException("O cartão selecionado está inativo.");
    }
    Instant now = clock.instant();
    BigDecimal installmentAmount = regularInstallmentAmount(totalAmount, normalizedInstallmentCount);
    CreditCardPurchase purchase = new CreditCardPurchase(
        card,
        normalizeRequired(description),
        totalAmount,
        purchaseDate,
        normalizedType,
        normalizedInstallmentCount,
        installmentAmount,
        ownerUserId,
        now);
    createInstallments(purchase, card, totalAmount, installmentAmount, normalizedInstallmentCount, now);
    CreditCardPurchase saved = purchaseRepository.saveAndFlush(purchase);
    registerSinglePurchaseDebitIfNeeded(
        saved,
        normalizedType,
        financialAccountId,
        totalAmount,
        purchaseDate);
    return purchaseResponseDTOFactory.createCreditCardPurchaseResponseDTO(
        purchaseResultFactory.createCreditCardPurchaseResult(saved, YearMonth.now(clock)));
  }

  @Override
  @Transactional
  public CreditCardPurchaseResponseDTO updateCreditCardPurchase(
      UUID id,
      String description,
      UUID creditCardId,
      BigDecimal totalAmount,
      LocalDate purchaseDate,
      CreditCardPurchaseType purchaseType,
      UUID financialAccountId,
      Integer installmentCount) {
    CreditCardPurchase purchase = purchaseRepository.findByIdAndOwnerUserIdForUpdate(
            id,
            currentUserProvider.currentUserId())
        .orElseThrow(() -> new ResourceNotFoundException("Compra do cartão não encontrada."));
    CreditCardPurchaseType normalizedType = normalizePurchaseType(purchaseType);
    int normalizedInstallmentCount = normalizeInstallmentCount(normalizedType, installmentCount);
    validatePurchase(totalAmount, normalizedInstallmentCount);

    UUID ownerUserId = currentUserProvider.currentUserId();
    CreditCard card = cardRepository.findByIdAndOwnerUserIdForUpdate(creditCardId, ownerUserId)
        .orElseThrow(() -> new ResourceNotFoundException("Cartão de crédito não encontrado."));
    if (!card.isActive()) {
      throw new IllegalStateException("O cartão selecionado está inativo.");
    }

    reverseSinglePurchaseDebitIfNeeded(purchase);
    Instant now = clock.instant();
    BigDecimal installmentAmount = regularInstallmentAmount(totalAmount, normalizedInstallmentCount);
    List<CreditCardInstallmentStatus> previousStatuses = purchase.getInstallments().stream()
        .sorted(Comparator.comparingInt(CreditCardInstallment::getInstallmentNumber))
        .map(CreditCardInstallment::getStatus)
        .toList();
    purchase.updateDetails(
        card,
        normalizeRequired(description),
        totalAmount,
        purchaseDate,
        normalizedType,
        normalizedInstallmentCount,
        installmentAmount,
        now);
    purchase.replaceInstallments(
        createReplacementInstallments(
            purchase,
            card,
            totalAmount,
            installmentAmount,
            normalizedInstallmentCount,
            previousStatuses,
            now),
        now);
    registerSinglePurchaseDebitIfNeeded(
        purchase,
        normalizedType,
        financialAccountId,
        totalAmount,
        purchaseDate);
    return purchaseResponseDTOFactory.createCreditCardPurchaseResponseDTO(
        purchaseResultFactory.createCreditCardPurchaseResult(purchase, YearMonth.now(clock)));
  }

  @Override
  @Transactional
  public CreditCardMonthlyPurchaseInstallmentResponseDTO updateCreditCardInstallmentAmount(
      UUID installmentId,
      BigDecimal amount) {
    validateAmount(amount);
    CreditCardInstallment installment = findInstallmentForUpdate(installmentId);
    if (installment.getStatus() == CreditCardInstallmentStatus.PAID) {
      throw new IllegalStateException(
          "Volte a parcela para pendente antes de alterar o valor desta competência.");
    }
    Instant now = clock.instant();
    installment.updateAmount(amount, now);
    installment.getPurchase().refreshTotals(now);
    YearMonth competence = YearMonth.of(
        installment.getCompetenceYear(),
        installment.getCompetenceMonth());
    return monthlyPurchasesResponseDTOFactory.createCreditCardMonthlyPurchaseInstallmentResponseDTO(
        createMonthlyPurchaseInstallmentResult(installment.getPurchase(), installment, competence));
  }

  @Override
  @Transactional
  public CreditCardMonthlyPurchaseInstallmentResponseDTO updateCreditCardInstallmentStatus(
      UUID installmentId,
      CreditCardInstallmentStatus status) {
    CreditCardInstallment installment = findInstallmentForUpdate(installmentId);
    if (installment.getStatus() != status) {
      Instant now = clock.instant();
      installment.updateStatus(status, now);
      installment.getPurchase().refreshTotals(now);
    }
    YearMonth competence = YearMonth.of(
        installment.getCompetenceYear(),
        installment.getCompetenceMonth());
    return monthlyPurchasesResponseDTOFactory.createCreditCardMonthlyPurchaseInstallmentResponseDTO(
        createMonthlyPurchaseInstallmentResult(installment.getPurchase(), installment, competence));
  }

  @Override
  @Transactional
  public void deleteCreditCardPurchase(UUID id) {
    CreditCardPurchase purchase = purchaseRepository.findByIdAndOwnerUserIdForUpdate(
            id,
            currentUserProvider.currentUserId())
        .orElseThrow(() -> new ResourceNotFoundException("Compra do cartão não encontrada."));
    reverseSinglePurchaseDebitIfNeeded(purchase);
    purchase.softDelete(clock.instant());
  }

  private CreditCardInstallment findInstallmentForUpdate(UUID id) {
    return installmentRepository.findByIdAndOwnerUserIdForUpdate(
            id,
            currentUserProvider.currentUserId())
        .orElseThrow(() -> new ResourceNotFoundException("Parcela do cartão não encontrada."));
  }

  private CreditCardMonthlyPurchaseInstallmentResult createMonthlyPurchaseInstallmentResult(
      CreditCardPurchase purchase,
      CreditCardInstallment installment,
      YearMonth selectedCompetence) {
    List<CreditCardInstallment> installments = purchase.getInstallments();
    int paidCount = countInstallmentsByStatus(installments, CreditCardInstallmentStatus.PAID);
    int pendingCount = installments.size() - paidCount;
    YearMonth endingCompetence = installments.stream()
        .map(item -> YearMonth.of(item.getCompetenceYear(), item.getCompetenceMonth()))
        .max(YearMonth::compareTo)
        .orElse(selectedCompetence);
    long monthsUntilEnd = ChronoUnit.MONTHS.between(selectedCompetence, endingCompetence);
    return new CreditCardMonthlyPurchaseInstallmentResult(
        installment.getId(),
        purchase.getId(),
        purchase.getDescription(),
        installment.getAmount(),
        purchase.getTotalAmount(),
        purchase.getPurchaseDate(),
        installment.getInstallmentNumber(),
        purchase.getInstallmentCount(),
        purchase.getCreditCard().getId(),
        purchase.getCreditCard().getName(),
        purchase.getCreditCard().getBrand(),
        purchase.getCreditCard().getLastFourDigits(),
        installment.getCompetenceYear(),
        installment.getCompetenceMonth(),
        installment.getDueDate(),
        installment.getStatus(),
        installment.getPaidAt(),
        paidCount,
        pendingCount,
        purchase.getInstallmentCount() - installment.getInstallmentNumber(),
        endingCompetence.getYear(),
        endingCompetence.getMonthValue(),
        Math.max(0, monthsUntilEnd),
        installment.getCreatedAt(),
        installment.getUpdatedAt());
  }

  private CreditCardMonthlyPurchaseSummaryResult createSummary(
      List<CreditCardMonthlyPurchaseInstallmentResult> items) {
    BigDecimal paidTotal = totalByStatus(items, CreditCardInstallmentStatus.PAID);
    BigDecimal pendingTotal = totalByStatus(items, CreditCardInstallmentStatus.PENDING);
    BigDecimal total = paidTotal.add(pendingTotal);
    int paidCount = countByStatus(items, CreditCardInstallmentStatus.PAID);
    int pendingCount = countByStatus(items, CreditCardInstallmentStatus.PENDING);
    return new CreditCardMonthlyPurchaseSummaryResult(
        total,
        paidTotal,
        pendingTotal,
        items.size(),
        paidCount,
        pendingCount,
        paidPercentage(paidTotal, total));
  }

  private BigDecimal totalByStatus(
      List<CreditCardMonthlyPurchaseInstallmentResult> items,
      CreditCardInstallmentStatus status) {
    return items.stream()
        .filter(item -> item.status() == status)
        .map(CreditCardMonthlyPurchaseInstallmentResult::amount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private int countByStatus(
      List<CreditCardMonthlyPurchaseInstallmentResult> items,
      CreditCardInstallmentStatus status) {
    return (int) items.stream().filter(item -> item.status() == status).count();
  }

  private int countInstallmentsByStatus(
      List<CreditCardInstallment> installments,
      CreditCardInstallmentStatus status) {
    return (int) installments.stream().filter(item -> item.getStatus() == status).count();
  }

  private BigDecimal paidPercentage(BigDecimal paidTotal, BigDecimal total) {
    if (total.signum() == 0) {
      return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
    return paidTotal
        .multiply(BigDecimal.valueOf(100))
        .divide(total, 2, RoundingMode.HALF_UP);
  }

  private void reverseSinglePurchaseDebitIfNeeded(CreditCardPurchase purchase) {
    if (purchase.getPurchaseType() != CreditCardPurchaseType.SINGLE) {
      return;
    }
    financialAccountService.reverseFinancialAccountDebitForReference(
        FinancialReferenceType.CREDIT_CARD_PURCHASE,
        purchase.getId(),
        LocalDate.now(clock),
        "Estorno de compra à vista no cartão - " + purchase.getDescription());
  }

  private void registerSinglePurchaseDebitIfNeeded(
      CreditCardPurchase purchase,
      CreditCardPurchaseType purchaseType,
      UUID financialAccountId,
      BigDecimal totalAmount,
      LocalDate purchaseDate) {
    if (purchaseType != CreditCardPurchaseType.SINGLE) {
      return;
    }
    if (financialAccountId == null) {
      throw new IllegalArgumentException("Selecione uma conta para pagar a compra à vista.");
    }
    financialAccountService.debitFinancialAccountForReference(
        financialAccountId,
        totalAmount,
        "Compra à vista no cartão - " + purchase.getDescription(),
        purchaseDate,
        FinancialReferenceType.CREDIT_CARD_PURCHASE,
        purchase.getId());
  }

  private BigDecimal pendingInstallmentTotal(CreditCardPurchase purchase) {
    return purchase.getInstallments().stream()
        .filter(installment -> installment.getStatus() == CreditCardInstallmentStatus.PENDING)
        .map(CreditCardInstallment::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private List<CreditCardInstallment> createReplacementInstallments(
      CreditCardPurchase purchase,
      CreditCard card,
      BigDecimal totalAmount,
      BigDecimal regularAmount,
      int installmentCount,
      List<CreditCardInstallmentStatus> previousStatuses,
      Instant now) {
    YearMonth firstCompetence = firstCompetence(purchase.getPurchaseDate(), card.getClosingDay());
    BigDecimal accumulated = BigDecimal.ZERO;
    List<CreditCardInstallment> installments = new java.util.ArrayList<>();
    for (int index = 1; index <= installmentCount; index++) {
      BigDecimal amount = index == installmentCount
          ? totalAmount.subtract(accumulated)
          : regularAmount;
      accumulated = accumulated.add(amount);
      YearMonth competence = firstCompetence.plusMonths(index - 1L);
      CreditCardInstallment installment = new CreditCardInstallment(
          purchase,
          index,
          amount,
          competence.getYear(),
          competence.getMonthValue(),
          dueDate(competence, card.getDueDay()),
          purchase.getOwnerUserId(),
          now);
      if (index <= previousStatuses.size()
          && previousStatuses.get(index - 1) == CreditCardInstallmentStatus.PAID) {
        installment.updateStatus(CreditCardInstallmentStatus.PAID, now);
      }
      installments.add(installment);
    }
    return installments;
  }

  private void createInstallments(
      CreditCardPurchase purchase,
      CreditCard card,
      BigDecimal totalAmount,
      BigDecimal regularAmount,
      int installmentCount,
      Instant now) {
    YearMonth firstCompetence = firstCompetence(purchase.getPurchaseDate(), card.getClosingDay());
    BigDecimal accumulated = BigDecimal.ZERO;
    for (int index = 1; index <= installmentCount; index++) {
      BigDecimal amount = index == installmentCount
          ? totalAmount.subtract(accumulated)
          : regularAmount;
      accumulated = accumulated.add(amount);
      YearMonth competence = firstCompetence.plusMonths(index - 1L);
      purchase.addInstallment(new CreditCardInstallment(
          purchase,
          index,
          amount,
          competence.getYear(),
          competence.getMonthValue(),
          dueDate(competence, card.getDueDay()),
          purchase.getOwnerUserId(),
          now));
    }
  }

  private YearMonth firstCompetence(LocalDate purchaseDate, int closingDay) {
    YearMonth purchaseMonth = YearMonth.from(purchaseDate);
    return purchaseDate.getDayOfMonth() <= closingDay ? purchaseMonth : purchaseMonth.plusMonths(1);
  }

  private LocalDate dueDate(YearMonth competence, int dueDay) {
    return competence.atDay(Math.min(dueDay, competence.lengthOfMonth()));
  }

  private BigDecimal regularInstallmentAmount(BigDecimal totalAmount, int installmentCount) {
    return totalAmount.divide(BigDecimal.valueOf(installmentCount), 2, RoundingMode.DOWN);
  }

  private void validatePeriod(int year, int month) {
    if (year < MIN_YEAR || year > MAX_YEAR) {
      throw new IllegalArgumentException("O ano deve estar entre 2000 e 2200.");
    }
    YearMonth.of(year, month);
  }

  private CreditCardPurchaseType normalizePurchaseType(CreditCardPurchaseType purchaseType) {
    return purchaseType == null ? CreditCardPurchaseType.INSTALLMENT : purchaseType;
  }

  private int normalizeInstallmentCount(
      CreditCardPurchaseType purchaseType,
      Integer installmentCount) {
    if (purchaseType == CreditCardPurchaseType.SINGLE) {
      return 1;
    }
    if (installmentCount == null) {
      throw new IllegalArgumentException("Informe o número de parcelas.");
    }
    return installmentCount;
  }

  private void validatePurchase(BigDecimal totalAmount, int installmentCount) {
    validateAmount(totalAmount);
    if (installmentCount < 1) {
      throw new IllegalArgumentException("A compra deve ter pelo menos uma parcela.");
    }
  }

  private void validateAmount(BigDecimal amount) {
    if (amount == null || amount.signum() <= 0) {
      throw new IllegalArgumentException("O valor deve ser maior que zero.");
    }
  }

  private String normalizeRequired(String value) {
    return value.trim();
  }
}

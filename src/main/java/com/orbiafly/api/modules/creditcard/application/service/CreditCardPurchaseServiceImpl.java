package com.orbiafly.api.modules.creditcard.application.service;

import com.orbiafly.api.infrastructure.security.CurrentUserProvider;
import com.orbiafly.api.modules.creditcard.application.contract.CreditCardPurchaseServiceInterface;
import com.orbiafly.api.modules.creditcard.application.factory.CreditCardPurchaseResultFactory;
import com.orbiafly.api.modules.creditcard.application.model.CreditCardPurchaseOverviewResult;
import com.orbiafly.api.modules.creditcard.application.model.CreditCardPurchaseResult;
import com.orbiafly.api.modules.creditcard.domain.CreditCard;
import com.orbiafly.api.modules.creditcard.domain.CreditCardInstallment;
import com.orbiafly.api.modules.creditcard.domain.CreditCardPurchase;
import com.orbiafly.api.modules.creditcard.domain.CreditCardPurchaseStatus;
import com.orbiafly.api.modules.creditcard.infrastructure.persistence.CreditCardPurchaseRepositoryInterface;
import com.orbiafly.api.modules.creditcard.infrastructure.persistence.CreditCardRepositoryInterface;
import com.orbiafly.api.modules.creditcard.web.dto.response.CreditCardPurchaseOverviewResponseDTO;
import com.orbiafly.api.modules.creditcard.web.dto.response.CreditCardPurchaseResponseDTO;
import com.orbiafly.api.modules.creditcard.web.factory.CreditCardPurchaseOverviewResponseDTOFactory;
import com.orbiafly.api.modules.creditcard.web.factory.CreditCardPurchaseResponseDTOFactory;
import com.orbiafly.api.modules.fixedexpense.application.ResourceNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreditCardPurchaseServiceImpl implements CreditCardPurchaseServiceInterface {

  private final CreditCardRepositoryInterface cardRepository;
  private final CreditCardPurchaseRepositoryInterface purchaseRepository;
  private final CreditCardPurchaseResultFactory purchaseResultFactory;
  private final CreditCardPurchaseResponseDTOFactory purchaseResponseDTOFactory;
  private final CreditCardPurchaseOverviewResponseDTOFactory overviewResponseDTOFactory;
  private final CurrentUserProvider currentUserProvider;
  private final Clock clock;

  public CreditCardPurchaseServiceImpl(
      CreditCardRepositoryInterface cardRepository,
      CreditCardPurchaseRepositoryInterface purchaseRepository,
      CreditCardPurchaseResultFactory purchaseResultFactory,
      CreditCardPurchaseResponseDTOFactory purchaseResponseDTOFactory,
      CreditCardPurchaseOverviewResponseDTOFactory overviewResponseDTOFactory,
      CurrentUserProvider currentUserProvider,
      Clock clock) {
    this.cardRepository = cardRepository;
    this.purchaseRepository = purchaseRepository;
    this.purchaseResultFactory = purchaseResultFactory;
    this.purchaseResponseDTOFactory = purchaseResponseDTOFactory;
    this.overviewResponseDTOFactory = overviewResponseDTOFactory;
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
        .filter(purchase -> purchase.status() == CreditCardPurchaseStatus.EM_ANDAMENTO)
        .map(CreditCardPurchaseResult::totalAmount)
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
  public CreditCardPurchaseResponseDTO getCreditCardPurchase(UUID id) {
    return purchaseRepository.findByIdAndOwnerUserIdAndDeletedAtIsNull(
            id,
            currentUserProvider.currentUserId())
        .map(purchase -> purchaseResultFactory.createCreditCardPurchaseResult(
            purchase,
            YearMonth.now(clock)))
        .map(purchaseResponseDTOFactory::createCreditCardPurchaseResponseDTO)
        .orElseThrow(() -> new ResourceNotFoundException("Compra parcelada não encontrada."));
  }

  @Override
  @Transactional
  public CreditCardPurchaseResponseDTO createCreditCardPurchase(
      String description,
      UUID creditCardId,
      BigDecimal totalAmount,
      LocalDate purchaseDate,
      int installmentCount) {
    validatePurchase(totalAmount, installmentCount);
    UUID ownerUserId = currentUserProvider.currentUserId();
    CreditCard card = cardRepository.findByIdAndOwnerUserIdForUpdate(creditCardId, ownerUserId)
        .orElseThrow(() -> new ResourceNotFoundException("Cartão de crédito não encontrado."));
    if (!card.isActive()) {
      throw new IllegalStateException("O cartão selecionado está inativo.");
    }
    BigDecimal committed = purchaseRepository.findCommittedAmountByCardId(
        card.getId(),
        ownerUserId);
    if (committed.add(totalAmount).compareTo(card.getTotalLimit()) > 0) {
      throw new IllegalStateException("Limite disponível insuficiente para registrar a compra.");
    }

    Instant now = clock.instant();
    BigDecimal installmentAmount = regularInstallmentAmount(totalAmount, installmentCount);
    CreditCardPurchase purchase = new CreditCardPurchase(
        card,
        normalizeRequired(description),
        totalAmount,
        purchaseDate,
        installmentCount,
        installmentAmount,
        ownerUserId,
        now);
    createInstallments(purchase, card, totalAmount, installmentAmount, installmentCount, now);
    CreditCardPurchase saved = purchaseRepository.save(purchase);
    return purchaseResponseDTOFactory.createCreditCardPurchaseResponseDTO(
        purchaseResultFactory.createCreditCardPurchaseResult(saved, YearMonth.now(clock)));
  }

  @Override
  @Transactional
  public void deleteCreditCardPurchase(UUID id) {
    CreditCardPurchase purchase = purchaseRepository.findByIdAndOwnerUserIdForUpdate(
            id,
            currentUserProvider.currentUserId())
        .orElseThrow(() -> new ResourceNotFoundException("Compra parcelada não encontrada."));
    purchase.softDelete(clock.instant());
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

  private void validatePurchase(BigDecimal totalAmount, int installmentCount) {
    if (totalAmount.signum() <= 0) {
      throw new IllegalArgumentException("O valor total deve ser maior que zero.");
    }
    if (installmentCount < 1) {
      throw new IllegalArgumentException("A compra deve ter pelo menos uma parcela.");
    }
  }

  private String normalizeRequired(String value) {
    return value.trim();
  }
}

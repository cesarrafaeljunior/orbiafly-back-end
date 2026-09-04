package com.orbiafly.api.modules.creditcard.application.service;

import com.orbiafly.api.infrastructure.security.CurrentUserProvider;
import com.orbiafly.api.modules.creditcard.application.contract.CreditCardServiceInterface;
import com.orbiafly.api.modules.creditcard.application.factory.CreditCardResultFactory;
import com.orbiafly.api.modules.creditcard.application.model.CreditCardOverviewResult;
import com.orbiafly.api.modules.creditcard.application.model.CreditCardResult;
import com.orbiafly.api.modules.creditcard.domain.CreditCard;
import com.orbiafly.api.modules.creditcard.domain.CreditCardBrand;
import com.orbiafly.api.modules.creditcard.infrastructure.persistence.CreditCardPurchaseRepositoryInterface;
import com.orbiafly.api.modules.creditcard.infrastructure.persistence.CreditCardRepositoryInterface;
import com.orbiafly.api.modules.creditcard.web.dto.response.CreditCardOverviewResponseDTO;
import com.orbiafly.api.modules.creditcard.web.dto.response.CreditCardResponseDTO;
import com.orbiafly.api.modules.creditcard.web.factory.CreditCardOverviewResponseDTOFactory;
import com.orbiafly.api.modules.creditcard.web.factory.CreditCardResponseDTOFactory;
import com.orbiafly.api.modules.fixedexpense.application.ResourceNotFoundException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreditCardServiceImpl implements CreditCardServiceInterface {

  private final CreditCardRepositoryInterface cardRepository;
  private final CreditCardPurchaseRepositoryInterface purchaseRepository;
  private final CreditCardResultFactory cardResultFactory;
  private final CreditCardResponseDTOFactory cardResponseDTOFactory;
  private final CreditCardOverviewResponseDTOFactory overviewResponseDTOFactory;
  private final CurrentUserProvider currentUserProvider;
  private final Clock clock;

  public CreditCardServiceImpl(
      CreditCardRepositoryInterface cardRepository,
      CreditCardPurchaseRepositoryInterface purchaseRepository,
      CreditCardResultFactory cardResultFactory,
      CreditCardResponseDTOFactory cardResponseDTOFactory,
      CreditCardOverviewResponseDTOFactory overviewResponseDTOFactory,
      CurrentUserProvider currentUserProvider,
      Clock clock) {
    this.cardRepository = cardRepository;
    this.purchaseRepository = purchaseRepository;
    this.cardResultFactory = cardResultFactory;
    this.cardResponseDTOFactory = cardResponseDTOFactory;
    this.overviewResponseDTOFactory = overviewResponseDTOFactory;
    this.currentUserProvider = currentUserProvider;
    this.clock = clock;
  }

  @Override
  @Transactional(readOnly = true)
  public CreditCardOverviewResponseDTO listCreditCards(boolean includeInactive) {
    UUID ownerUserId = currentUserProvider.currentUserId();
    List<CreditCard> cards = includeInactive
        ? cardRepository.findAllByOwnerUserIdAndDeletedAtIsNullOrderByActiveDescNameAsc(ownerUserId)
        : cardRepository.findAllByOwnerUserIdAndActiveTrueAndDeletedAtIsNullOrderByNameAsc(
            ownerUserId);
    Map<UUID, BigDecimal> committedAmounts = committedAmounts(cards, ownerUserId);
    List<CreditCardResult> views = cards.stream()
        .map(card -> cardResultFactory.createCreditCardResult(
            card,
            committedAmounts.getOrDefault(card.getId(), BigDecimal.ZERO)))
        .toList();
    BigDecimal totalLimit = views.stream()
        .filter(CreditCardResult::active)
        .map(CreditCardResult::totalLimit)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal usedLimit = views.stream()
        .filter(CreditCardResult::active)
        .map(CreditCardResult::usedLimit)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    int activeCount = (int) views.stream().filter(CreditCardResult::active).count();
    CreditCardOverviewResult overview = new CreditCardOverviewResult(
        totalLimit,
        usedLimit,
        totalLimit.subtract(usedLimit),
        activeCount,
        views);
    return overviewResponseDTOFactory.createCreditCardOverviewResponseDTO(overview);
  }

  @Override
  @Transactional(readOnly = true)
  public CreditCardResponseDTO getCreditCard(UUID id) {
    UUID ownerUserId = currentUserProvider.currentUserId();
    return cardRepository.findByIdAndOwnerUserIdAndDeletedAtIsNull(id, ownerUserId)
        .map(card -> cardResultFactory.createCreditCardResult(
            card,
            purchaseRepository.findCommittedAmountByCardId(card.getId(), ownerUserId)))
        .map(cardResponseDTOFactory::createCreditCardResponseDTO)
        .orElseThrow(() -> new ResourceNotFoundException("Cartão de crédito não encontrado."));
  }

  @Override
  @Transactional
  public CreditCardResponseDTO createCreditCard(
      String name,
      CreditCardBrand brand,
      String lastFourDigits,
      BigDecimal totalLimit,
      int closingDay,
      int dueDay) {
    validateTotalLimit(totalLimit);
    Instant now = clock.instant();
    CreditCard card = new CreditCard(
        normalizeRequired(name),
        brand,
        normalizeLastFourDigits(lastFourDigits),
        totalLimit,
        closingDay,
        dueDay,
        currentUserProvider.currentUserId(),
        now);
    return cardResponseDTOFactory.createCreditCardResponseDTO(
        cardResultFactory.createCreditCardResult(cardRepository.save(card), BigDecimal.ZERO));
  }

  @Override
  @Transactional
  public CreditCardResponseDTO updateCreditCard(
      UUID id,
      String name,
      CreditCardBrand brand,
      String lastFourDigits,
      BigDecimal totalLimit,
      int closingDay,
      int dueDay) {
    validateTotalLimit(totalLimit);
    CreditCard card = findCardForUpdate(id);
    card.updateCreditCard(
        normalizeRequired(name),
        brand,
        normalizeLastFourDigits(lastFourDigits),
        totalLimit,
        closingDay,
        dueDay,
        clock.instant());
    return cardResponseDTOFactory.createCreditCardResponseDTO(
        cardResultFactory.createCreditCardResult(
            card,
            purchaseRepository.findCommittedAmountByCardId(
                card.getId(),
                card.getOwnerUserId())));
  }

  @Override
  @Transactional
  public CreditCardResponseDTO deactivateCreditCard(UUID id) {
    CreditCard card = findCardForUpdate(id);
    card.deactivate(clock.instant());
    return cardResponseDTOFactory.createCreditCardResponseDTO(
        cardResultFactory.createCreditCardResult(
            card,
            purchaseRepository.findCommittedAmountByCardId(
                card.getId(),
                card.getOwnerUserId())));
  }

  @Override
  @Transactional
  public CreditCardResponseDTO reactivateCreditCard(UUID id) {
    CreditCard card = findCardForUpdate(id);
    card.reactivateCreditCard(clock.instant());
    return cardResponseDTOFactory.createCreditCardResponseDTO(
        cardResultFactory.createCreditCardResult(
            card,
            purchaseRepository.findCommittedAmountByCardId(
                card.getId(),
                card.getOwnerUserId())));
  }

  @Override
  @Transactional
  public void deleteCreditCard(UUID id) {
    CreditCard card = findCardForUpdate(id);
    card.softDelete(clock.instant());
  }

  private CreditCard findCardForUpdate(UUID id) {
    return cardRepository.findByIdAndOwnerUserIdForUpdate(id, currentUserProvider.currentUserId())
        .orElseThrow(() -> new ResourceNotFoundException("Cartão de crédito não encontrado."));
  }

  private Map<UUID, BigDecimal> committedAmounts(List<CreditCard> cards, UUID ownerUserId) {
    if (cards.isEmpty()) {
      return Map.of();
    }
    List<UUID> cardIds = cards.stream().map(CreditCard::getId).toList();
    Map<UUID, BigDecimal> amounts = new HashMap<>();
    purchaseRepository.findCommittedAmountsByCardIds(cardIds, ownerUserId).forEach(row ->
        amounts.put((UUID) row[0], (BigDecimal) row[1]));
    return amounts;
  }

  private void validateTotalLimit(BigDecimal totalLimit) {
    if (totalLimit.signum() < 0) {
      throw new IllegalArgumentException("O limite total não pode ser negativo.");
    }
  }

  private String normalizeRequired(String value) {
    return value.trim();
  }

  private String normalizeLastFourDigits(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }
}

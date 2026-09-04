package com.orbiafly.api.modules.creditcard.infrastructure.persistence;

import com.orbiafly.api.modules.creditcard.domain.CreditCardPurchase;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CreditCardPurchaseRepositoryInterface
    extends JpaRepository<CreditCardPurchase, UUID> {

  @EntityGraph(attributePaths = {"creditCard", "installments"})
  List<CreditCardPurchase> findAllByOwnerUserIdAndDeletedAtIsNullOrderByPurchaseDateDescCreatedAtDesc(
      UUID ownerUserId);

  @EntityGraph(attributePaths = {"creditCard", "installments"})
  Optional<CreditCardPurchase> findByIdAndOwnerUserIdAndDeletedAtIsNull(
      UUID id,
      UUID ownerUserId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @EntityGraph(attributePaths = {"creditCard", "installments"})
  @Query("""
      SELECT purchase
      FROM CreditCardPurchase purchase
      WHERE purchase.id = :id
        AND purchase.ownerUserId = :ownerUserId
        AND purchase.deletedAt IS NULL
      """)
  Optional<CreditCardPurchase> findByIdAndOwnerUserIdForUpdate(
      @Param("id") UUID id,
      @Param("ownerUserId") UUID ownerUserId);

  @Query("""
      SELECT COALESCE(SUM(purchase.totalAmount), 0)
      FROM CreditCardPurchase purchase
      WHERE purchase.creditCard.id = :cardId
        AND purchase.ownerUserId = :ownerUserId
        AND purchase.deletedAt IS NULL
      """)
  BigDecimal findCommittedAmountByCardId(
      @Param("cardId") UUID cardId,
      @Param("ownerUserId") UUID ownerUserId);

  @Query("""
      SELECT purchase.creditCard.id, COALESCE(SUM(purchase.totalAmount), 0)
      FROM CreditCardPurchase purchase
      WHERE purchase.creditCard.id IN :cardIds
        AND purchase.ownerUserId = :ownerUserId
        AND purchase.deletedAt IS NULL
      GROUP BY purchase.creditCard.id
      """)
  List<Object[]> findCommittedAmountsByCardIds(
      @Param("cardIds") List<UUID> cardIds,
      @Param("ownerUserId") UUID ownerUserId);
}

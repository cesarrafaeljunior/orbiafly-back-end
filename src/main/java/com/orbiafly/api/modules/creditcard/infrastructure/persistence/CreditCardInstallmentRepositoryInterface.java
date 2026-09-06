package com.orbiafly.api.modules.creditcard.infrastructure.persistence;

import com.orbiafly.api.modules.creditcard.domain.CreditCardInstallment;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CreditCardInstallmentRepositoryInterface
    extends JpaRepository<CreditCardInstallment, UUID> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("""
      SELECT installment
      FROM CreditCardInstallment installment
      JOIN FETCH installment.purchase purchase
      JOIN FETCH purchase.creditCard
      LEFT JOIN FETCH purchase.installments purchaseInstallment
      WHERE installment.id = :id
        AND installment.ownerUserId = :ownerUserId
        AND purchase.deletedAt IS NULL
      """)
  Optional<CreditCardInstallment> findByIdAndOwnerUserIdForUpdate(
      @Param("id") UUID id,
      @Param("ownerUserId") UUID ownerUserId);
}

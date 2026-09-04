package com.orbiafly.api.modules.installmentexpense.infrastructure.persistence;

import com.orbiafly.api.modules.installmentexpense.domain.InstallmentExpenseInstallment;
import com.orbiafly.api.modules.fixedexpense.domain.PaymentStatus;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InstallmentExpenseInstallmentRepositoryInterface
    extends JpaRepository<InstallmentExpenseInstallment, UUID> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT installment FROM InstallmentExpenseInstallment installment JOIN FETCH installment.purchase WHERE installment.id = :id AND installment.owner.id = :ownerId")
  Optional<InstallmentExpenseInstallment> findByIdAndOwnerIdForUpdate(
      @Param("id") UUID id,
      @Param("ownerId") UUID ownerId);

  @Modifying
  @Query("""
      UPDATE InstallmentExpenseInstallment installment
      SET installment.status = :status,
          installment.paidAt = NULL,
          installment.financialAccountId = NULL,
          installment.paymentTransactionId = NULL,
          installment.updatedAt = :updatedAt
      WHERE installment.financialAccountId = :financialAccountId AND installment.owner.id = :ownerId
      """)
  int unlinkFinancialAccountPayments(
      @Param("financialAccountId") UUID financialAccountId,
      @Param("ownerId") UUID ownerId,
      @Param("status") PaymentStatus status,
      @Param("updatedAt") Instant updatedAt);
}

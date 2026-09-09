package com.orbiafly.api.modules.installmentexpense.infrastructure.persistence;

import com.orbiafly.api.modules.installmentexpense.domain.InstallmentExpenseInstallment;
import com.orbiafly.api.modules.fixedexpense.domain.PaymentStatus;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InstallmentExpenseInstallmentRepositoryInterface
    extends JpaRepository<InstallmentExpenseInstallment, UUID> {

  @Query("""
      SELECT installment
      FROM InstallmentExpenseInstallment installment
      JOIN FETCH installment.purchase
      WHERE installment.ownerUserId = :ownerUserId
        AND installment.status = :status
        AND installment.dueDate BETWEEN :startDate AND :endDate
      ORDER BY installment.dueDate ASC, installment.purchase.description ASC
      """)
  List<InstallmentExpenseInstallment> findMonthlyPending(
      @Param("ownerUserId") UUID ownerUserId,
      @Param("status") PaymentStatus status,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("""
      SELECT installment
      FROM InstallmentExpenseInstallment installment
      JOIN FETCH installment.purchase
      WHERE installment.id = :id
        AND installment.ownerUserId = :ownerUserId
      """)
  Optional<InstallmentExpenseInstallment> findByIdAndOwnerUserIdForUpdate(
      @Param("id") UUID id,
      @Param("ownerUserId") UUID ownerUserId);

  @Modifying
  @Query("""
      UPDATE InstallmentExpenseInstallment installment
      SET installment.status = :status,
          installment.paidAt = NULL,
          installment.financialAccountId = NULL,
          installment.paymentTransactionId = NULL,
          installment.updatedAt = :updatedAt
      WHERE installment.financialAccountId = :financialAccountId
        AND installment.ownerUserId = :ownerUserId
      """)
  int unlinkFinancialAccountPayments(
      @Param("financialAccountId") UUID financialAccountId,
      @Param("ownerUserId") UUID ownerUserId,
      @Param("status") PaymentStatus status,
      @Param("updatedAt") Instant updatedAt);
}

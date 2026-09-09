package com.orbiafly.api.modules.oneoffexpense.infrastructure.persistence;

import com.orbiafly.api.modules.oneoffexpense.domain.OneOffExpense;
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

public interface OneOffExpenseRepositoryInterface extends JpaRepository<OneOffExpense, UUID> {

  @Query("""
      select expense from OneOffExpense expense
      join fetch expense.category
      where expense.ownerUserId = :ownerUserId
        and expense.expenseDate between :startDate and :endDate
      order by expense.expenseDate asc, expense.name asc
      """)
  List<OneOffExpense> findMonthly(
      @Param("ownerUserId") UUID ownerUserId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);

  @Query("""
      select expense from OneOffExpense expense
      join fetch expense.category
      where expense.id = :id and expense.ownerUserId = :ownerUserId
      """)
  Optional<OneOffExpense> findByIdAndOwnerUserId(
      @Param("id") UUID id,
      @Param("ownerUserId") UUID ownerUserId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("""
      select expense from OneOffExpense expense
      join fetch expense.category
      where expense.id = :id and expense.ownerUserId = :ownerUserId
      """)
  Optional<OneOffExpense> findByIdAndOwnerUserIdForUpdate(
      @Param("id") UUID id,
      @Param("ownerUserId") UUID ownerUserId);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("""
      update OneOffExpense expense
      set expense.status = com.orbiafly.api.modules.fixedexpense.domain.PaymentStatus.PENDING,
          expense.paidAt = null,
          expense.financialAccountId = null,
          expense.paymentTransactionId = null,
          expense.updatedAt = :now
      where expense.ownerUserId = :ownerUserId
        and expense.financialAccountId = :accountId
      """)
  int unlinkFinancialAccountPayments(
      @Param("accountId") UUID accountId,
      @Param("ownerUserId") UUID ownerUserId,
      @Param("now") Instant now);
}

package com.orbiafly.api.modules.fixedexpense.infrastructure.persistence;

import com.orbiafly.api.modules.fixedexpense.domain.FixedExpenseOccurrence;
import com.orbiafly.api.modules.fixedexpense.domain.PaymentStatus;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FixedExpenseOccurrenceRepositoryInterface
    extends JpaRepository<FixedExpenseOccurrence, UUID> {

  @Modifying
  @Query(value = """
      INSERT INTO fixed_expense_occurrence (
        id,
        fixed_expense_id,
        occurrence_year,
        occurrence_month,
        name,
        amount,
        due_day,
        category_id,
        category_name,
        owner_user_id,
        status,
        paid_at,
        created_at,
        updated_at
      )
      SELECT
        gen_random_uuid(),
        expense.id,
        :year,
        :month,
        expense.name,
        expense.default_amount,
        expense.due_day,
        category.id,
        category.name,
        expense.owner_user_id,
        'PENDING',
        NULL,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
      FROM fixed_expense expense
      JOIN category ON category.id = expense.category_id
      WHERE expense.active = TRUE
        AND expense.owner_user_id = :ownerUserId
      ON CONFLICT (fixed_expense_id, occurrence_year, occurrence_month) DO NOTHING
      """, nativeQuery = true)
  int materializeActiveExpenses(
      @Param("ownerUserId") UUID ownerUserId,
      @Param("year") int year,
      @Param("month") int month);

  @Query("""
      SELECT occurrence
      FROM FixedExpenseOccurrence occurrence
      JOIN FETCH occurrence.fixedExpense expense
      JOIN FETCH expense.category
      WHERE occurrence.ownerUserId = :ownerUserId
        AND occurrence.year = :year
        AND occurrence.month = :month
      ORDER BY occurrence.dueDay ASC, occurrence.name ASC
      """)
  List<FixedExpenseOccurrence> findMonthly(
      @Param("ownerUserId") UUID ownerUserId,
      @Param("year") int year,
      @Param("month") int month);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("""
      SELECT occurrence
      FROM FixedExpenseOccurrence occurrence
      WHERE occurrence.id = :id
        AND occurrence.ownerUserId = :ownerUserId
      """)
  Optional<FixedExpenseOccurrence> findByIdAndOwnerUserIdForUpdate(
      @Param("id") UUID id,
      @Param("ownerUserId") UUID ownerUserId);

  @Modifying
  @Query("""
      UPDATE FixedExpenseOccurrence occurrence
      SET occurrence.name = :name,
          occurrence.amount = :amount,
          occurrence.dueDay = :dueDay,
          occurrence.categoryId = :categoryId,
          occurrence.categoryName = :categoryName,
          occurrence.updatedAt = :updatedAt
      WHERE occurrence.fixedExpense.id = :fixedExpenseId
        AND occurrence.ownerUserId = :ownerUserId
        AND occurrence.status = :status
      """)
  int syncPendingOccurrences(
      @Param("fixedExpenseId") UUID fixedExpenseId,
      @Param("ownerUserId") UUID ownerUserId,
      @Param("status") PaymentStatus status,
      @Param("name") String name,
      @Param("amount") java.math.BigDecimal amount,
      @Param("dueDay") int dueDay,
      @Param("categoryId") UUID categoryId,
      @Param("categoryName") String categoryName,
      @Param("updatedAt") Instant updatedAt);

  @Modifying
  @Query("""
      UPDATE FixedExpenseOccurrence occurrence
      SET occurrence.status = :status,
          occurrence.paidAt = NULL,
          occurrence.financialAccountId = NULL,
          occurrence.paymentTransactionId = NULL,
          occurrence.updatedAt = :updatedAt
      WHERE occurrence.financialAccountId = :financialAccountId
        AND occurrence.ownerUserId = :ownerUserId
      """)
  int unlinkFinancialAccountPayments(
      @Param("financialAccountId") UUID financialAccountId,
      @Param("ownerUserId") UUID ownerUserId,
      @Param("status") PaymentStatus status,
      @Param("updatedAt") Instant updatedAt);
}

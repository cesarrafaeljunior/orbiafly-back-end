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
        'PENDING',
        NULL,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
      FROM fixed_expense expense
      JOIN category ON category.id = expense.category_id
      WHERE expense.active = TRUE
      ON CONFLICT (fixed_expense_id, occurrence_year, occurrence_month) DO NOTHING
      """, nativeQuery = true)
  int materializeActiveExpenses(@Param("year") int year, @Param("month") int month);

  @Query("""
      SELECT occurrence
      FROM FixedExpenseOccurrence occurrence
      JOIN FETCH occurrence.fixedExpense expense
      JOIN FETCH expense.category
      WHERE occurrence.year = :year AND occurrence.month = :month
      ORDER BY occurrence.dueDay ASC, occurrence.name ASC
      """)
  List<FixedExpenseOccurrence> findMonthly(
      @Param("year") int year,
      @Param("month") int month);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT occurrence FROM FixedExpenseOccurrence occurrence WHERE occurrence.id = :id")
  Optional<FixedExpenseOccurrence> findByIdForUpdate(@Param("id") UUID id);

  @Modifying
  @Query("""
      UPDATE FixedExpenseOccurrence occurrence
      SET occurrence.status = :status,
          occurrence.paidAt = NULL,
          occurrence.financialAccountId = NULL,
          occurrence.paymentTransactionId = NULL,
          occurrence.updatedAt = :updatedAt
      WHERE occurrence.financialAccountId = :financialAccountId
      """)
  int unlinkFinancialAccountPayments(
      @Param("financialAccountId") UUID financialAccountId,
      @Param("status") PaymentStatus status,
      @Param("updatedAt") Instant updatedAt);
}

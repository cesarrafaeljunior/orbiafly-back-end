package com.orbiafly.api.modules.financialaccount.infrastructure.persistence;

import com.orbiafly.api.modules.financialaccount.domain.FinancialTransaction;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FinancialTransactionRepositoryInterface
    extends JpaRepository<FinancialTransaction, UUID> {

  List<FinancialTransaction> findAllByFinancialAccountIdOrderByOccurredOnDescCreatedAtDesc(
      UUID financialAccountId);

  boolean existsByReversalOfId(UUID transactionId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT transaction FROM FinancialTransaction transaction WHERE transaction.id = :id")
  Optional<FinancialTransaction> findByIdForUpdate(@Param("id") UUID id);

  @Query(value = """
      SELECT
        financial_account_id AS accountId,
        COALESCE(SUM(
          CASE transaction_type
            WHEN 'CREDIT' THEN amount
            WHEN 'DEBIT' THEN -amount
            ELSE amount
          END
        ), 0) AS balanceDelta
      FROM financial_transaction
      GROUP BY financial_account_id
      """, nativeQuery = true)
  List<AccountBalanceProjectionInterface> findBalanceDeltas();

  @Query(value = """
      SELECT COALESCE(SUM(
        CASE transaction_type
          WHEN 'CREDIT' THEN amount
          WHEN 'DEBIT' THEN -amount
          ELSE amount
        END
      ), 0)
      FROM financial_transaction
      WHERE financial_account_id = :accountId
      """, nativeQuery = true)
  BigDecimal findBalanceDelta(@Param("accountId") UUID accountId);
}

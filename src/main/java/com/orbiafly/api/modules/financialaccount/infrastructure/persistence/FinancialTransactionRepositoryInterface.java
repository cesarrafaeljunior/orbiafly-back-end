package com.orbiafly.api.modules.financialaccount.infrastructure.persistence;

import com.orbiafly.api.modules.financialaccount.domain.FinancialTransaction;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FinancialTransactionRepositoryInterface
    extends JpaRepository<FinancialTransaction, UUID> {

  List<FinancialTransaction> findAllByFinancialAccountIdAndOwnerIdOrderByOccurredOnDescCreatedAtDesc(
      UUID financialAccountId, UUID ownerId);

  boolean existsByReversalOfId(UUID transactionId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT transaction FROM FinancialTransaction transaction WHERE transaction.id = :id AND transaction.owner.id = :ownerId")
  Optional<FinancialTransaction> findByIdAndOwnerIdForUpdate(@Param("id") UUID id, @Param("ownerId") UUID ownerId);

  Optional<FinancialTransaction> findByIdAndOwnerId(UUID id, UUID ownerId);

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
      WHERE user_id = :ownerId
      GROUP BY financial_account_id
      """, nativeQuery = true)
  List<AccountBalanceProjectionInterface> findBalanceDeltas(@Param("ownerId") UUID ownerId);

  @Query(value = """
      SELECT COALESCE(SUM(
        CASE transaction_type
          WHEN 'CREDIT' THEN amount
          WHEN 'DEBIT' THEN -amount
          ELSE amount
        END
      ), 0)
      FROM financial_transaction
      WHERE financial_account_id = :accountId AND user_id = :ownerId
      """, nativeQuery = true)
  BigDecimal findBalanceDelta(@Param("accountId") UUID accountId, @Param("ownerId") UUID ownerId);

  @Modifying
  @Query("DELETE FROM FinancialTransaction transaction WHERE transaction.financialAccount.id = :accountId AND transaction.owner.id = :ownerId")
  int deleteAllByFinancialAccountIdAndOwnerId(@Param("accountId") UUID accountId, @Param("ownerId") UUID ownerId);
}

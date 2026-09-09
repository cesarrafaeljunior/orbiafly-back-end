package com.orbiafly.api.modules.financialaccount.infrastructure.persistence;

import com.orbiafly.api.modules.financialaccount.domain.FinancialReferenceType;
import com.orbiafly.api.modules.financialaccount.domain.FinancialTransaction;
import com.orbiafly.api.modules.financialaccount.domain.FinancialTransactionType;
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

  List<FinancialTransaction> findAllByOwnerUserIdAndFinancialAccountIdOrderByOccurredOnDescCreatedAtDesc(
      UUID ownerUserId,
      UUID financialAccountId);

  boolean existsByReversalOfId(UUID transactionId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT transaction FROM FinancialTransaction transaction WHERE transaction.id = :id AND transaction.ownerUserId = :ownerUserId")
  Optional<FinancialTransaction> findByIdAndOwnerUserIdForUpdate(
      @Param("id") UUID id,
      @Param("ownerUserId") UUID ownerUserId);


  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("""
      SELECT transaction
      FROM FinancialTransaction transaction
      WHERE transaction.ownerUserId = :ownerUserId
        AND transaction.referenceType = :referenceType
        AND transaction.referenceId = :referenceId
        AND transaction.type = :type
      ORDER BY transaction.createdAt DESC
      """)
  List<FinancialTransaction> findDebitByReferenceForUpdate(
      @Param("ownerUserId") UUID ownerUserId,
      @Param("referenceType") FinancialReferenceType referenceType,
      @Param("referenceId") UUID referenceId,
      @Param("type") FinancialTransactionType type);

  @Query(value = """
      SELECT
        financial_account_id AS accountId,
        COALESCE(SUM(
          CASE transaction_type
            WHEN 'CREDIT' THEN amount
            WHEN 'TRANSFER_IN' THEN amount
            WHEN 'DEBIT' THEN -amount
            WHEN 'TRANSFER_OUT' THEN -amount
            ELSE amount
          END
        ), 0) AS balanceDelta
      FROM financial_transaction
      WHERE owner_user_id = :ownerUserId
      GROUP BY financial_account_id
      """, nativeQuery = true)
  List<AccountBalanceProjectionInterface> findBalanceDeltas(@Param("ownerUserId") UUID ownerUserId);

  @Query(value = """
      SELECT COALESCE(SUM(
        CASE transaction_type
          WHEN 'CREDIT' THEN amount
          WHEN 'TRANSFER_IN' THEN amount
          WHEN 'DEBIT' THEN -amount
          WHEN 'TRANSFER_OUT' THEN -amount
          ELSE amount
        END
      ), 0)
      FROM financial_transaction
      WHERE financial_account_id = :accountId
        AND owner_user_id = :ownerUserId
      """, nativeQuery = true)
  BigDecimal findBalanceDelta(
      @Param("accountId") UUID accountId,
      @Param("ownerUserId") UUID ownerUserId);

  @Modifying
  @Query("DELETE FROM FinancialTransaction transaction WHERE transaction.financialAccount.id = :accountId AND transaction.ownerUserId = :ownerUserId")
  int deleteAllByFinancialAccountIdAndOwnerUserId(
      @Param("accountId") UUID accountId,
      @Param("ownerUserId") UUID ownerUserId);
}

package com.orbiafly.api.modules.expectedincome.infrastructure.persistence;

import com.orbiafly.api.modules.expectedincome.domain.ExpectedIncome;
import com.orbiafly.api.modules.expectedincome.domain.ExpectedIncomeStatus;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExpectedIncomeRepositoryInterface extends JpaRepository<ExpectedIncome, UUID> {

  @EntityGraph(attributePaths = {"financialAccount"})
  List<ExpectedIncome> findAllByOwnerUserIdAndDeletedAtIsNullOrderByExpectedDateAscCreatedAtDesc(
      UUID ownerUserId);

  @EntityGraph(attributePaths = {"financialAccount"})
  Optional<ExpectedIncome> findByIdAndOwnerUserIdAndDeletedAtIsNull(UUID id, UUID ownerUserId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @EntityGraph(attributePaths = {"financialAccount"})
  @Query("""
      SELECT income
      FROM ExpectedIncome income
      WHERE income.id = :id
        AND income.ownerUserId = :ownerUserId
        AND income.deletedAt IS NULL
      """)
  Optional<ExpectedIncome> findByIdAndOwnerUserIdForUpdate(
      @Param("id") UUID id,
      @Param("ownerUserId") UUID ownerUserId);

  @EntityGraph(attributePaths = {"financialAccount"})
  List<ExpectedIncome> findAllByOwnerUserIdAndStatusAndDeletedAtIsNullAndExpectedDateBetweenOrderByExpectedDateAscDescriptionAsc(
      UUID ownerUserId,
      ExpectedIncomeStatus status,
      LocalDate startDate,
      LocalDate endDate);
}

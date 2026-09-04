package com.orbiafly.api.modules.loan.infrastructure.persistence;

import com.orbiafly.api.modules.loan.domain.Loan;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoanRepositoryInterface extends JpaRepository<Loan, UUID> {

  @EntityGraph(attributePaths = {"installments", "entries"})
  List<Loan> findAllByOwnerUserIdAndDeletedAtIsNullOrderByStatusAscStartDateDescCreatedAtDesc(
      UUID ownerUserId);

  @EntityGraph(attributePaths = {"installments", "entries"})
  Optional<Loan> findByIdAndOwnerUserIdAndDeletedAtIsNull(UUID id, UUID ownerUserId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @EntityGraph(attributePaths = {"installments", "entries"})
  @Query("""
      SELECT loan
      FROM Loan loan
      WHERE loan.id = :id
        AND loan.ownerUserId = :ownerUserId
        AND loan.deletedAt IS NULL
      """)
  Optional<Loan> findByIdAndOwnerUserIdForUpdate(
      @Param("id") UUID id,
      @Param("ownerUserId") UUID ownerUserId);
}

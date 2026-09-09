package com.orbiafly.api.modules.accounttransfer.infrastructure.persistence;

import com.orbiafly.api.modules.accounttransfer.domain.AccountTransfer;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountTransferRepositoryInterface extends JpaRepository<AccountTransfer, UUID> {

  @EntityGraph(attributePaths = {"sourceAccount", "destinationAccount"})
  List<AccountTransfer> findAllByOwnerUserIdOrderByTransferDateDescCreatedAtDesc(UUID ownerUserId);

  @EntityGraph(attributePaths = {"sourceAccount", "destinationAccount"})
  Optional<AccountTransfer> findByIdAndOwnerUserId(UUID id, UUID ownerUserId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @EntityGraph(attributePaths = {"sourceAccount", "destinationAccount"})
  @Query("""
      SELECT transfer
      FROM AccountTransfer transfer
      WHERE transfer.id = :id
        AND transfer.ownerUserId = :ownerUserId
      """)
  Optional<AccountTransfer> findByIdAndOwnerUserIdForUpdate(
      @Param("id") UUID id,
      @Param("ownerUserId") UUID ownerUserId);
}

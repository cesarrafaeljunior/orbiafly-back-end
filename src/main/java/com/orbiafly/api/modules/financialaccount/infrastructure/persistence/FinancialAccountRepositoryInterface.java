package com.orbiafly.api.modules.financialaccount.infrastructure.persistence;

import com.orbiafly.api.modules.financialaccount.domain.FinancialAccount;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FinancialAccountRepositoryInterface extends JpaRepository<FinancialAccount, UUID> {

  List<FinancialAccount> findAllByOwnerIdAndActiveTrueOrderByNameAsc(UUID ownerId);

  List<FinancialAccount> findAllByOwnerIdOrderByActiveDescNameAsc(UUID ownerId);

  boolean existsByOwnerIdAndActiveTrue(UUID ownerId);

  Optional<FinancialAccount> findByIdAndOwnerId(UUID id, UUID ownerId);

  List<FinancialAccount> findAllByIdInAndOwnerId(Collection<UUID> ids, UUID ownerId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT account FROM FinancialAccount account WHERE account.id = :id AND account.owner.id = :ownerId")
  Optional<FinancialAccount> findByIdAndOwnerIdForUpdate(@Param("id") UUID id, @Param("ownerId") UUID ownerId);
}

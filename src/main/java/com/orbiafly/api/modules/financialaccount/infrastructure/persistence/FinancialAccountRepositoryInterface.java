package com.orbiafly.api.modules.financialaccount.infrastructure.persistence;

import com.orbiafly.api.modules.financialaccount.domain.FinancialAccount;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FinancialAccountRepositoryInterface extends JpaRepository<FinancialAccount, UUID> {

  List<FinancialAccount> findAllByActiveTrueOrderByNameAsc();

  List<FinancialAccount> findAllByOrderByActiveDescNameAsc();

  boolean existsByActiveTrue();

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT account FROM FinancialAccount account WHERE account.id = :id")
  Optional<FinancialAccount> findByIdForUpdate(@Param("id") UUID id);
}

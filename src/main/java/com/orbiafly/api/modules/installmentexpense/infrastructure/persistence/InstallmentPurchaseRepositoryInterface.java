package com.orbiafly.api.modules.installmentexpense.infrastructure.persistence;

import com.orbiafly.api.modules.installmentexpense.domain.InstallmentPurchase;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstallmentPurchaseRepositoryInterface extends JpaRepository<InstallmentPurchase, UUID> {

  @EntityGraph(attributePaths = "installments")
  List<InstallmentPurchase> findAllByOwnerIdOrderByCreatedAtDesc(UUID ownerId);

  @EntityGraph(attributePaths = "installments")
  Optional<InstallmentPurchase> findByIdAndOwnerId(UUID id, UUID ownerId);
}

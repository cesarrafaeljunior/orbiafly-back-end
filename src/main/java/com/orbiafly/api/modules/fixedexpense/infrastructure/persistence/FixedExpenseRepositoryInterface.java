package com.orbiafly.api.modules.fixedexpense.infrastructure.persistence;

import com.orbiafly.api.modules.fixedexpense.domain.FixedExpense;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FixedExpenseRepositoryInterface extends JpaRepository<FixedExpense, UUID> {

  @EntityGraph(attributePaths = "category")
  List<FixedExpense> findAllByOwnerIdOrderByNameAsc(UUID ownerId);

  Optional<FixedExpense> findByIdAndOwnerId(UUID id, UUID ownerId);
}

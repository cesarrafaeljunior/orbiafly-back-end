package com.orbiafly.api.modules.fixedexpense.infrastructure.persistence;

import com.orbiafly.api.modules.fixedexpense.domain.FixedExpense;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FixedExpenseRepository extends JpaRepository<FixedExpense, UUID> {

  @EntityGraph(attributePaths = "category")
  List<FixedExpense> findAllByOrderByNameAsc();
}

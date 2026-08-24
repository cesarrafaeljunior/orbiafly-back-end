package com.orbiafly.api.modules.fixedexpense.infrastructure.persistence;

import com.orbiafly.api.modules.fixedexpense.domain.Category;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

  List<Category> findAllByOrderByNameAsc();
}

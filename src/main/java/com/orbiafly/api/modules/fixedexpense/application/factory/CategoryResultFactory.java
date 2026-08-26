package com.orbiafly.api.modules.fixedexpense.application.factory;

import com.orbiafly.api.modules.fixedexpense.application.model.CategoryResult;
import com.orbiafly.api.modules.fixedexpense.domain.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryResultFactory {

  public CategoryResult createCategoryResult(Category category) {
    return new CategoryResult(category.getId(), category.getName());
  }
}

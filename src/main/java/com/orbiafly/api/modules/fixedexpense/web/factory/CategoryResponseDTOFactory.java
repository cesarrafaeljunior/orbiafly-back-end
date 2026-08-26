package com.orbiafly.api.modules.fixedexpense.web.factory;

import com.orbiafly.api.modules.fixedexpense.application.model.CategoryResult;
import com.orbiafly.api.modules.fixedexpense.web.dto.response.CategoryResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class CategoryResponseDTOFactory {

  public CategoryResponseDTO createCategoryResponseDTO(CategoryResult category) {
    return new CategoryResponseDTO(category.id(), category.name());
  }
}

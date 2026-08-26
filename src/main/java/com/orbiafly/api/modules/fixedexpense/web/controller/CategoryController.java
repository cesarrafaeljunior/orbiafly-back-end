package com.orbiafly.api.modules.fixedexpense.web.controller;

import com.orbiafly.api.modules.fixedexpense.application.contract.FixedExpenseServiceInterface;
import com.orbiafly.api.modules.fixedexpense.web.dto.response.CategoryResponseDTO;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

  private final FixedExpenseServiceInterface service;

  public CategoryController(FixedExpenseServiceInterface service) {
    this.service = service;
  }

  @GetMapping
  public List<CategoryResponseDTO> listFixedExpenseCategories() {
    return service.listFixedExpenseCategories();
  }
}

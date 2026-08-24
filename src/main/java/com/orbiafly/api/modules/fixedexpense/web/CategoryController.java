package com.orbiafly.api.modules.fixedexpense.web;

import com.orbiafly.api.modules.fixedexpense.application.CategoryView;
import com.orbiafly.api.modules.fixedexpense.application.FixedExpenseService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

  private final FixedExpenseService service;

  public CategoryController(FixedExpenseService service) {
    this.service = service;
  }

  @GetMapping
  public List<CategoryView> list() {
    return service.listCategories();
  }
}

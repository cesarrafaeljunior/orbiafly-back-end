package com.orbiafly.api.modules.fixedexpense.web;

import com.orbiafly.api.modules.fixedexpense.application.FixedExpenseService;
import com.orbiafly.api.modules.fixedexpense.application.FixedExpenseView;
import com.orbiafly.api.modules.fixedexpense.application.MonthlyExpensesView;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/fixed-expenses")
public class FixedExpenseController {

  private final FixedExpenseService service;

  public FixedExpenseController(FixedExpenseService service) {
    this.service = service;
  }

  @GetMapping
  public List<FixedExpenseView> list() {
    return service.listFixedExpenses();
  }

  @PostMapping
  public ResponseEntity<FixedExpenseView> create(
      @Valid @RequestBody CreateFixedExpenseRequest request) {
    FixedExpenseView created = service.create(
        request.name(),
        request.defaultAmount(),
        request.dueDay(),
        request.categoryId());
    return ResponseEntity.created(URI.create("/api/v1/fixed-expenses/" + created.id()))
        .body(created);
  }

  @PutMapping("/{id}")
  public FixedExpenseView update(
      @PathVariable UUID id,
      @Valid @RequestBody UpdateFixedExpenseRequest request) {
    return service.update(
        id,
        request.name(),
        request.defaultAmount(),
        request.dueDay(),
        request.categoryId(),
        request.active());
  }

  @GetMapping("/monthly")
  public MonthlyExpensesView monthly(
      @RequestParam @Min(2000) @Max(2200) int year,
      @RequestParam @Min(1) @Max(12) int month) {
    return service.getMonthly(year, month);
  }
}

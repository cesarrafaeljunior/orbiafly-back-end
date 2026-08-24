package com.orbiafly.api.modules.fixedexpense.web;

import com.orbiafly.api.modules.fixedexpense.application.FixedExpenseService;
import com.orbiafly.api.modules.fixedexpense.application.MonthlyExpenseView;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/fixed-expense-occurrences")
public class FixedExpenseOccurrenceController {

  private final FixedExpenseService service;

  public FixedExpenseOccurrenceController(FixedExpenseService service) {
    this.service = service;
  }

  @PatchMapping("/{id}/amount")
  public MonthlyExpenseView updateAmount(
      @PathVariable UUID id,
      @Valid @RequestBody UpdateOccurrenceAmountRequest request) {
    return service.updateOccurrenceAmount(id, request.amount());
  }

  @PatchMapping("/{id}/status")
  public MonthlyExpenseView updateStatus(
      @PathVariable UUID id,
      @Valid @RequestBody UpdateOccurrenceStatusRequest request) {
    return service.updateOccurrenceStatus(id, request.status());
  }
}

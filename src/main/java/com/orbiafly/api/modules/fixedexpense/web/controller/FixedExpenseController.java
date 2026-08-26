package com.orbiafly.api.modules.fixedexpense.web.controller;

import com.orbiafly.api.modules.fixedexpense.application.contract.FixedExpenseServiceInterface;
import com.orbiafly.api.modules.fixedexpense.web.dto.request.CreateFixedExpenseRequestDTO;
import com.orbiafly.api.modules.fixedexpense.web.dto.request.UpdateFixedExpenseRequestDTO;
import com.orbiafly.api.modules.fixedexpense.web.dto.response.FixedExpenseResponseDTO;
import com.orbiafly.api.modules.fixedexpense.web.dto.response.MonthlyExpensesResponseDTO;
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

  private final FixedExpenseServiceInterface service;

  public FixedExpenseController(FixedExpenseServiceInterface service) {
    this.service = service;
  }

  @GetMapping
  public List<FixedExpenseResponseDTO> listFixedExpenses() {
    return service.listFixedExpenses();
  }

  @PostMapping
  public ResponseEntity<FixedExpenseResponseDTO> createFixedExpense(
      @Valid @RequestBody CreateFixedExpenseRequestDTO request) {
    FixedExpenseResponseDTO createdExpense = service.createFixedExpense(
        request.name(),
        request.defaultAmount(),
        request.dueDay(),
        request.categoryId());
    return ResponseEntity.created(URI.create("/api/v1/fixed-expenses/" + createdExpense.id()))
        .body(createdExpense);
  }

  @PutMapping("/{id}")
  public FixedExpenseResponseDTO updateFixedExpense(
      @PathVariable UUID id,
      @Valid @RequestBody UpdateFixedExpenseRequestDTO request) {
    return service.updateFixedExpense(
        id,
        request.name(),
        request.defaultAmount(),
        request.dueDay(),
        request.categoryId(),
        request.active());
  }

  @GetMapping("/monthly")
  public MonthlyExpensesResponseDTO getMonthlyFixedExpenses(
      @RequestParam @Min(2000) @Max(2200) int year,
      @RequestParam @Min(1) @Max(12) int month) {
    return service.getMonthlyFixedExpenses(year, month);
  }
}

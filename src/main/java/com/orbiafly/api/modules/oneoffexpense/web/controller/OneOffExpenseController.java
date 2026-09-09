package com.orbiafly.api.modules.oneoffexpense.web.controller;

import com.orbiafly.api.modules.oneoffexpense.application.contract.OneOffExpenseServiceInterface;
import com.orbiafly.api.modules.oneoffexpense.web.dto.request.CreateOneOffExpenseRequestDTO;
import com.orbiafly.api.modules.oneoffexpense.web.dto.request.RegisterOneOffExpensePaymentRequestDTO;
import com.orbiafly.api.modules.oneoffexpense.web.dto.request.UpdateOneOffExpenseRequestDTO;
import com.orbiafly.api.modules.oneoffexpense.web.dto.request.UpdateOneOffExpenseStatusRequestDTO;
import com.orbiafly.api.modules.oneoffexpense.web.dto.response.MonthlyOneOffExpensesResponseDTO;
import com.orbiafly.api.modules.oneoffexpense.web.dto.response.OneOffExpensePaymentResponseDTO;
import com.orbiafly.api.modules.oneoffexpense.web.dto.response.OneOffExpenseResponseDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/one-off-expenses")
public class OneOffExpenseController {
  private final OneOffExpenseServiceInterface service;

  public OneOffExpenseController(OneOffExpenseServiceInterface service) { this.service = service; }

  @GetMapping
  public MonthlyOneOffExpensesResponseDTO getMonthlyOneOffExpenses(
      @RequestParam @Min(2000) @Max(2200) int year,
      @RequestParam @Min(1) @Max(12) int month) {
    return service.getMonthlyOneOffExpenses(year, month);
  }

  @PostMapping
  public ResponseEntity<OneOffExpenseResponseDTO> createOneOffExpense(
      @Valid @RequestBody CreateOneOffExpenseRequestDTO request) {
    OneOffExpenseResponseDTO created = service.createOneOffExpense(
        request.name(), request.amount(), request.expenseDate(), request.categoryId());
    return ResponseEntity.created(URI.create("/api/v1/one-off-expenses/" + created.id())).body(created);
  }

  @PutMapping("/{id}")
  public OneOffExpenseResponseDTO updateOneOffExpense(
      @PathVariable UUID id, @Valid @RequestBody UpdateOneOffExpenseRequestDTO request) {
    return service.updateOneOffExpense(
        id, request.name(), request.amount(), request.expenseDate(), request.categoryId());
  }

  @PatchMapping("/{id}/status")
  public OneOffExpenseResponseDTO updateOneOffExpenseStatus(
      @PathVariable UUID id, @Valid @RequestBody UpdateOneOffExpenseStatusRequestDTO request) {
    return service.updateOneOffExpenseStatus(id, request.status());
  }

  @PostMapping("/{id}/payment")
  public OneOffExpensePaymentResponseDTO registerOneOffExpensePayment(
      @PathVariable UUID id, @Valid @RequestBody RegisterOneOffExpensePaymentRequestDTO request) {
    return service.registerOneOffExpensePayment(id, request.financialAccountId(), request.paymentDate());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteOneOffExpense(@PathVariable UUID id) {
    service.deleteOneOffExpense(id);
    return ResponseEntity.noContent().build();
  }
}

package com.orbiafly.api.modules.fixedexpense.web.controller;

import com.orbiafly.api.modules.fixedexpense.application.contract.FixedExpenseServiceInterface;
import com.orbiafly.api.modules.fixedexpense.web.dto.request.RegisterOccurrencePaymentRequestDTO;
import com.orbiafly.api.modules.fixedexpense.web.dto.request.UpdateOccurrenceAmountRequestDTO;
import com.orbiafly.api.modules.fixedexpense.web.dto.request.UpdateOccurrenceStatusRequestDTO;
import com.orbiafly.api.modules.fixedexpense.web.dto.response.FixedExpensePaymentResponseDTO;
import com.orbiafly.api.modules.fixedexpense.web.dto.response.MonthlyExpenseResponseDTO;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/fixed-expense-occurrences")
public class FixedExpenseOccurrenceController {

  private final FixedExpenseServiceInterface service;

  public FixedExpenseOccurrenceController(FixedExpenseServiceInterface service) {
    this.service = service;
  }

  @PatchMapping("/{id}/amount")
  public MonthlyExpenseResponseDTO updateFixedExpenseOccurrenceAmount(
      @PathVariable UUID id,
      @Valid @RequestBody UpdateOccurrenceAmountRequestDTO request) {
    return service.updateFixedExpenseOccurrenceAmount(id, request.amount());
  }

  @PatchMapping("/{id}/status")
  public MonthlyExpenseResponseDTO updateFixedExpenseOccurrenceStatus(
      @PathVariable UUID id,
      @Valid @RequestBody UpdateOccurrenceStatusRequestDTO request) {
    return service.updateFixedExpenseOccurrenceStatus(id, request.status());
  }

  @PostMapping("/{id}/payment")
  public FixedExpensePaymentResponseDTO registerFixedExpenseOccurrencePayment(
      @PathVariable UUID id,
      @Valid @RequestBody RegisterOccurrencePaymentRequestDTO request) {
    return service.registerFixedExpenseOccurrencePayment(
        id,
        request.financialAccountId(),
        request.paymentDate());
  }
}

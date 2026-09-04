package com.orbiafly.api.modules.installmentexpense.web.controller;

import com.orbiafly.api.modules.installmentexpense.application.contract.InstallmentExpenseServiceInterface;
import com.orbiafly.api.modules.installmentexpense.web.dto.request.CreateInstallmentPurchaseRequestDTO;
import com.orbiafly.api.modules.installmentexpense.web.dto.request.PayInstallmentRequestDTO;
import com.orbiafly.api.modules.installmentexpense.web.dto.response.InstallmentPaymentResponseDTO;
import com.orbiafly.api.modules.installmentexpense.web.dto.response.InstallmentPurchaseResponseDTO;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/installment-expenses")
public class InstallmentExpenseController {

  private final InstallmentExpenseServiceInterface service;

  public InstallmentExpenseController(InstallmentExpenseServiceInterface service) {
    this.service = service;
  }

  @GetMapping
  public List<InstallmentPurchaseResponseDTO> listInstallmentPurchases() {
    return service.listInstallmentPurchases();
  }

  @GetMapping("/{id}")
  public InstallmentPurchaseResponseDTO getInstallmentPurchase(@PathVariable UUID id) {
    return service.getInstallmentPurchase(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public InstallmentPurchaseResponseDTO createInstallmentPurchase(
      @Valid @RequestBody CreateInstallmentPurchaseRequestDTO request) {
    return service.createInstallmentPurchase(
        request.description(),
        request.totalAmount(),
        request.installmentCount(),
        request.firstInstallmentDate());
  }

  @PostMapping("/installments/{id}/payment")
  public InstallmentPaymentResponseDTO payInstallment(
      @PathVariable UUID id,
      @Valid @RequestBody PayInstallmentRequestDTO request) {
    return service.payInstallment(id, request.financialAccountId(), request.paymentDate());
  }
}

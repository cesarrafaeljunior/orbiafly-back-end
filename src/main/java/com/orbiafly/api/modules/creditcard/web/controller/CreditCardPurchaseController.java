package com.orbiafly.api.modules.creditcard.web.controller;

import com.orbiafly.api.modules.creditcard.application.contract.CreditCardPurchaseServiceInterface;
import com.orbiafly.api.modules.creditcard.web.dto.request.CreateCreditCardPurchaseRequestDTO;
import com.orbiafly.api.modules.creditcard.web.dto.request.UpdateCreditCardInstallmentAmountRequestDTO;
import com.orbiafly.api.modules.creditcard.web.dto.request.UpdateCreditCardPurchaseRequestDTO;
import com.orbiafly.api.modules.creditcard.web.dto.request.UpdateCreditCardInstallmentStatusRequestDTO;
import com.orbiafly.api.modules.creditcard.web.dto.response.CreditCardMonthlyPurchaseInstallmentResponseDTO;
import com.orbiafly.api.modules.creditcard.web.dto.response.CreditCardMonthlyPurchasesResponseDTO;
import com.orbiafly.api.modules.creditcard.web.dto.response.CreditCardPurchaseOverviewResponseDTO;
import com.orbiafly.api.modules.creditcard.web.dto.response.CreditCardPurchaseResponseDTO;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/credit-card-purchases")
public class CreditCardPurchaseController {

  private final CreditCardPurchaseServiceInterface service;

  public CreditCardPurchaseController(CreditCardPurchaseServiceInterface service) {
    this.service = service;
  }

  @GetMapping
  public CreditCardPurchaseOverviewResponseDTO listCreditCardPurchases() {
    return service.listCreditCardPurchases();
  }

  @GetMapping("/monthly")
  public CreditCardMonthlyPurchasesResponseDTO getMonthlyCreditCardPurchases(
      @RequestParam int year,
      @RequestParam int month) {
    return service.getMonthlyCreditCardPurchases(year, month);
  }

  @GetMapping("/{id}")
  public CreditCardPurchaseResponseDTO getCreditCardPurchase(@PathVariable UUID id) {
    return service.getCreditCardPurchase(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CreditCardPurchaseResponseDTO createCreditCardPurchase(
      @Valid @RequestBody CreateCreditCardPurchaseRequestDTO request) {
    return service.createCreditCardPurchase(
        request.description(),
        request.creditCardId(),
        request.totalAmount(),
        request.purchaseDate(),
        request.purchaseType(),
        request.financialAccountId(),
        request.installmentCount());
  }

  @PutMapping("/{id}")
  public CreditCardPurchaseResponseDTO updateCreditCardPurchase(
      @PathVariable UUID id,
      @Valid @RequestBody UpdateCreditCardPurchaseRequestDTO request) {
    return service.updateCreditCardPurchase(
        id,
        request.description(),
        request.creditCardId(),
        request.totalAmount(),
        request.purchaseDate(),
        request.purchaseType(),
        request.financialAccountId(),
        request.installmentCount());
  }

  @PatchMapping("/installments/{installmentId}/amount")
  public CreditCardMonthlyPurchaseInstallmentResponseDTO updateCreditCardInstallmentAmount(
      @PathVariable UUID installmentId,
      @Valid @RequestBody UpdateCreditCardInstallmentAmountRequestDTO request) {
    return service.updateCreditCardInstallmentAmount(installmentId, request.amount());
  }

  @PatchMapping("/installments/{installmentId}/status")
  public CreditCardMonthlyPurchaseInstallmentResponseDTO updateCreditCardInstallmentStatus(
      @PathVariable UUID installmentId,
      @Valid @RequestBody UpdateCreditCardInstallmentStatusRequestDTO request) {
    return service.updateCreditCardInstallmentStatus(installmentId, request.status());
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteCreditCardPurchase(@PathVariable UUID id) {
    service.deleteCreditCardPurchase(id);
  }
}

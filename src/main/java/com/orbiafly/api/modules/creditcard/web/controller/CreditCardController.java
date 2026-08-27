package com.orbiafly.api.modules.creditcard.web.controller;

import com.orbiafly.api.modules.creditcard.application.contract.CreditCardServiceInterface;
import com.orbiafly.api.modules.creditcard.web.dto.request.CreateCreditCardRequestDTO;
import com.orbiafly.api.modules.creditcard.web.dto.request.UpdateCreditCardRequestDTO;
import com.orbiafly.api.modules.creditcard.web.dto.response.CreditCardOverviewResponseDTO;
import com.orbiafly.api.modules.creditcard.web.dto.response.CreditCardResponseDTO;
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
@RequestMapping("/api/v1/credit-cards")
public class CreditCardController {

  private final CreditCardServiceInterface service;

  public CreditCardController(CreditCardServiceInterface service) {
    this.service = service;
  }

  @GetMapping
  public CreditCardOverviewResponseDTO listCreditCards(
      @RequestParam(defaultValue = "false") boolean includeInactive) {
    return service.listCreditCards(includeInactive);
  }

  @GetMapping("/{id}")
  public CreditCardResponseDTO getCreditCard(@PathVariable UUID id) {
    return service.getCreditCard(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CreditCardResponseDTO createCreditCard(
      @Valid @RequestBody CreateCreditCardRequestDTO request) {
    return service.createCreditCard(
        request.name(),
        request.brand(),
        request.lastFourDigits(),
        request.totalLimit(),
        request.closingDay(),
        request.dueDay());
  }

  @PutMapping("/{id}")
  public CreditCardResponseDTO updateCreditCard(
      @PathVariable UUID id,
      @Valid @RequestBody UpdateCreditCardRequestDTO request) {
    return service.updateCreditCard(
        id,
        request.name(),
        request.brand(),
        request.lastFourDigits(),
        request.totalLimit(),
        request.closingDay(),
        request.dueDay());
  }

  @PatchMapping("/{id}/deactivate")
  public CreditCardResponseDTO deactivateCreditCard(@PathVariable UUID id) {
    return service.deactivateCreditCard(id);
  }

  @PatchMapping("/{id}/reactivate")
  public CreditCardResponseDTO reactivateCreditCard(@PathVariable UUID id) {
    return service.reactivateCreditCard(id);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteCreditCard(@PathVariable UUID id) {
    service.deleteCreditCard(id);
  }
}

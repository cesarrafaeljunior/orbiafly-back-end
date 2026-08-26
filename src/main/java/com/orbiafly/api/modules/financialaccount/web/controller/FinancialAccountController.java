package com.orbiafly.api.modules.financialaccount.web.controller;

import com.orbiafly.api.modules.financialaccount.application.contract.FinancialAccountServiceInterface;
import com.orbiafly.api.modules.financialaccount.web.dto.request.AdjustBalanceRequestDTO;
import com.orbiafly.api.modules.financialaccount.web.dto.request.CreateFinancialAccountRequestDTO;
import com.orbiafly.api.modules.financialaccount.web.dto.request.UpdateFinancialAccountRequestDTO;
import com.orbiafly.api.modules.financialaccount.web.dto.response.AccountTransactionResponseDTO;
import com.orbiafly.api.modules.financialaccount.web.dto.response.FinancialAccountResponseDTO;
import com.orbiafly.api.modules.financialaccount.web.dto.response.FinancialAccountsOverviewResponseDTO;
import com.orbiafly.api.modules.financialaccount.web.dto.response.FinancialTransactionResponseDTO;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/api/v1/accounts")
public class FinancialAccountController {

  private final FinancialAccountServiceInterface service;

  public FinancialAccountController(FinancialAccountServiceInterface service) {
    this.service = service;
  }

  @GetMapping
  public FinancialAccountsOverviewResponseDTO listFinancialAccounts(
      @RequestParam(defaultValue = "false") boolean includeInactive) {
    return service.listFinancialAccounts(includeInactive);
  }

  @GetMapping("/{id}")
  public FinancialAccountResponseDTO getFinancialAccount(@PathVariable UUID id) {
    return service.getFinancialAccount(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public FinancialAccountResponseDTO createFinancialAccount(
      @Valid @RequestBody CreateFinancialAccountRequestDTO request) {
    FinancialAccountResponseDTO createdAccount = service.createFinancialAccount(
        request.name(),
        request.type(),
        request.institution(),
        request.openingBalance());
    return createdAccount;
  }

  @PutMapping("/{id}")
  public FinancialAccountResponseDTO updateFinancialAccount(
      @PathVariable UUID id,
      @Valid @RequestBody UpdateFinancialAccountRequestDTO request) {
    return service.updateFinancialAccount(
        id,
        request.name(),
        request.type(),
        request.institution());
  }

  @PatchMapping("/{id}/deactivate")
  public FinancialAccountResponseDTO deactivateFinancialAccount(@PathVariable UUID id) {
    return service.deactivateFinancialAccount(id);
  }

  @PatchMapping("/{id}/reactivate")
  public FinancialAccountResponseDTO reactivateFinancialAccount(@PathVariable UUID id) {
    return service.reactivateFinancialAccount(id);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteFinancialAccount(@PathVariable UUID id) {
    service.deleteFinancialAccount(id);
  }

  @GetMapping("/{id}/transactions")
  public List<FinancialTransactionResponseDTO> listFinancialAccountTransactions(
      @PathVariable UUID id) {
    return service.listFinancialAccountTransactions(id);
  }

  @PostMapping("/{id}/adjustments")
  public AccountTransactionResponseDTO adjustFinancialAccountBalance(
      @PathVariable UUID id,
      @Valid @RequestBody AdjustBalanceRequestDTO request) {
    return service.adjustFinancialAccountBalance(
        id,
        request.targetBalance(),
        request.description());
  }
}

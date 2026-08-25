package com.orbiafly.api.modules.financialaccount.web;

import com.orbiafly.api.modules.financialaccount.application.AccountTransactionResult;
import com.orbiafly.api.modules.financialaccount.application.FinancialAccountService;
import com.orbiafly.api.modules.financialaccount.application.FinancialAccountView;
import com.orbiafly.api.modules.financialaccount.application.FinancialAccountsOverview;
import com.orbiafly.api.modules.financialaccount.application.FinancialTransactionView;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
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

  private final FinancialAccountService service;

  public FinancialAccountController(FinancialAccountService service) {
    this.service = service;
  }

  @GetMapping
  public FinancialAccountsOverview list(
      @RequestParam(defaultValue = "false") boolean includeInactive) {
    return service.list(includeInactive);
  }

  @GetMapping("/{id}")
  public FinancialAccountView get(@PathVariable UUID id) {
    return service.get(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public FinancialAccountView create(@Valid @RequestBody CreateFinancialAccountRequest request) {
    return service.create(
        request.name(),
        request.type(),
        request.institution(),
        request.openingBalance());
  }

  @PutMapping("/{id}")
  public FinancialAccountView update(
      @PathVariable UUID id,
      @Valid @RequestBody UpdateFinancialAccountRequest request) {
    return service.update(id, request.name(), request.type(), request.institution());
  }

  @PatchMapping("/{id}/deactivate")
  public FinancialAccountView deactivate(@PathVariable UUID id) {
    return service.deactivate(id);
  }

  @GetMapping("/{id}/transactions")
  public List<FinancialTransactionView> transactions(@PathVariable UUID id) {
    return service.listTransactions(id);
  }

  @PostMapping("/{id}/adjustments")
  public AccountTransactionResult adjust(
      @PathVariable UUID id,
      @Valid @RequestBody AdjustBalanceRequest request) {
    return service.adjustBalance(id, request.targetBalance(), request.description());
  }
}

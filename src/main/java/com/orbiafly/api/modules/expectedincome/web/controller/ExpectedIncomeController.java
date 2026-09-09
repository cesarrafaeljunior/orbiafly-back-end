package com.orbiafly.api.modules.expectedincome.web.controller;

import com.orbiafly.api.modules.expectedincome.application.contract.ExpectedIncomeServiceInterface;
import com.orbiafly.api.modules.expectedincome.web.dto.request.CreateExpectedIncomeRequestDTO;
import com.orbiafly.api.modules.expectedincome.web.dto.request.ReceiveExpectedIncomeRequestDTO;
import com.orbiafly.api.modules.expectedincome.web.dto.request.UpdateExpectedIncomeRequestDTO;
import com.orbiafly.api.modules.expectedincome.web.dto.response.ExpectedIncomeOverviewResponseDTO;
import com.orbiafly.api.modules.expectedincome.web.dto.response.ExpectedIncomeReceiptResponseDTO;
import com.orbiafly.api.modules.expectedincome.web.dto.response.ExpectedIncomeResponseDTO;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/expected-incomes")
public class ExpectedIncomeController {

  private final ExpectedIncomeServiceInterface service;

  public ExpectedIncomeController(ExpectedIncomeServiceInterface service) {
    this.service = service;
  }

  @GetMapping
  public ExpectedIncomeOverviewResponseDTO listExpectedIncomes() {
    return service.listExpectedIncomes();
  }

  @GetMapping("/{id}")
  public ExpectedIncomeResponseDTO getExpectedIncome(@PathVariable UUID id) {
    return service.getExpectedIncome(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ExpectedIncomeResponseDTO createExpectedIncome(
      @Valid @RequestBody CreateExpectedIncomeRequestDTO request) {
    return service.createExpectedIncome(
        request.description(),
        request.amount(),
        request.expectedDate(),
        request.financialAccountId(),
        request.notes());
  }

  @PutMapping("/{id}")
  public ExpectedIncomeResponseDTO updateExpectedIncome(
      @PathVariable UUID id,
      @Valid @RequestBody UpdateExpectedIncomeRequestDTO request) {
    return service.updateExpectedIncome(
        id,
        request.description(),
        request.amount(),
        request.expectedDate(),
        request.financialAccountId(),
        request.notes());
  }

  @PostMapping("/{id}/receive")
  public ExpectedIncomeReceiptResponseDTO receiveExpectedIncome(
      @PathVariable UUID id,
      @RequestBody(required = false) ReceiveExpectedIncomeRequestDTO request) {
    return service.receiveExpectedIncome(
        id,
        request == null ? null : request.financialAccountId(),
        request == null ? null : request.receivedDate());
  }

  @PatchMapping("/{id}/cancel")
  public ExpectedIncomeResponseDTO cancelExpectedIncome(@PathVariable UUID id) {
    return service.cancelExpectedIncome(id);
  }
}

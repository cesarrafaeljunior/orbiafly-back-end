package com.orbiafly.api.modules.loan.web.controller;

import com.orbiafly.api.modules.loan.application.contract.LoanServiceInterface;
import com.orbiafly.api.modules.loan.web.dto.request.CreateLoanRequestDTO;
import com.orbiafly.api.modules.loan.web.dto.request.RegisterLoanEntryRequestDTO;
import com.orbiafly.api.modules.loan.web.dto.response.LoanOverviewResponseDTO;
import com.orbiafly.api.modules.loan.web.dto.response.LoanResponseDTO;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/loans")
public class LoanController {

  private final LoanServiceInterface service;

  public LoanController(LoanServiceInterface service) {
    this.service = service;
  }

  @GetMapping
  public LoanOverviewResponseDTO listLoans() {
    return service.listLoans();
  }

  @GetMapping("/{id}")
  public LoanResponseDTO getLoan(@PathVariable UUID id) {
    return service.getLoan(id);
  }

  @PostMapping
  public ResponseEntity<LoanResponseDTO> createLoan(
      @Valid @RequestBody CreateLoanRequestDTO request) {
    LoanResponseDTO response = service.createLoan(
        request.name(),
        request.direction(),
        request.counterparty(),
        request.totalAmount(),
        request.startDate(),
        request.installmentCount(),
        request.installmentAmount(),
        request.firstDueDate(),
        request.interestRate(),
        request.notes());
    return ResponseEntity.created(URI.create("/api/v1/loans/" + response.id())).body(response);
  }

  @PostMapping("/{id}/entries")
  public LoanResponseDTO registerLoanEntry(
      @PathVariable UUID id,
      @Valid @RequestBody RegisterLoanEntryRequestDTO request) {
    return service.registerLoanEntry(
        id,
        request.financialAccountId(),
        request.amount(),
        request.occurredOn());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteLoan(@PathVariable UUID id) {
    service.deleteLoan(id);
    return ResponseEntity.noContent().build();
  }
}

package com.orbiafly.api.modules.accounttransfer.web.controller;

import com.orbiafly.api.modules.accounttransfer.application.contract.AccountTransferServiceInterface;
import com.orbiafly.api.modules.accounttransfer.web.dto.request.CreateAccountTransferRequestDTO;
import com.orbiafly.api.modules.accounttransfer.web.dto.response.AccountTransferCreationResponseDTO;
import com.orbiafly.api.modules.accounttransfer.web.dto.response.AccountTransferOverviewResponseDTO;
import com.orbiafly.api.modules.accounttransfer.web.dto.response.AccountTransferResponseDTO;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/account-transfers")
public class AccountTransferController {

  private final AccountTransferServiceInterface service;

  public AccountTransferController(AccountTransferServiceInterface service) {
    this.service = service;
  }

  @GetMapping
  public AccountTransferOverviewResponseDTO listAccountTransfers() {
    return service.listAccountTransfers();
  }

  @GetMapping("/{id}")
  public AccountTransferResponseDTO getAccountTransfer(@PathVariable UUID id) {
    return service.getAccountTransfer(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public AccountTransferCreationResponseDTO createAccountTransfer(
      @Valid @RequestBody CreateAccountTransferRequestDTO request) {
    return service.createAccountTransfer(
        request.sourceAccountId(),
        request.destinationAccountId(),
        request.amount(),
        request.transferDate(),
        request.description());
  }
}

package com.orbiafly.api.modules.accounttransfer.application.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AccountTransferResult(
    UUID id,
    UUID sourceAccountId,
    String sourceAccountName,
    UUID destinationAccountId,
    String destinationAccountName,
    BigDecimal amount,
    LocalDate transferDate,
    String description,
    UUID sourceTransactionId,
    UUID destinationTransactionId,
    Instant createdAt,
    Instant updatedAt) {
}

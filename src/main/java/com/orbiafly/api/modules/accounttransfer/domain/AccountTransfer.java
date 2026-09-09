package com.orbiafly.api.modules.accounttransfer.domain;

import com.orbiafly.api.modules.financialaccount.domain.FinancialAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "account_transfer")
public class AccountTransfer {

  @Id
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "source_account_id", nullable = false)
  private FinancialAccount sourceAccount;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "destination_account_id", nullable = false)
  private FinancialAccount destinationAccount;

  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal amount;

  @Column(name = "transfer_date", nullable = false)
  private LocalDate transferDate;

  @Column(nullable = false, length = 180)
  private String description;

  @Column(name = "source_transaction_id", unique = true)
  private UUID sourceTransactionId;

  @Column(name = "destination_transaction_id", unique = true)
  private UUID destinationTransactionId;

  @Column(name = "owner_user_id", nullable = false)
  private UUID ownerUserId;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected AccountTransfer() {
  }

  public AccountTransfer(
      FinancialAccount sourceAccount,
      FinancialAccount destinationAccount,
      BigDecimal amount,
      LocalDate transferDate,
      String description,
      UUID ownerUserId,
      Instant now) {
    this.id = UUID.randomUUID();
    this.sourceAccount = sourceAccount;
    this.destinationAccount = destinationAccount;
    this.amount = amount;
    this.transferDate = transferDate;
    this.description = description;
    this.ownerUserId = ownerUserId;
    this.createdAt = now;
    this.updatedAt = now;
  }

  public void attachTransactions(UUID sourceTransactionId, UUID destinationTransactionId, Instant now) {
    this.sourceTransactionId = sourceTransactionId;
    this.destinationTransactionId = destinationTransactionId;
    this.updatedAt = now;
  }

  public UUID getId() { return id; }
  public FinancialAccount getSourceAccount() { return sourceAccount; }
  public FinancialAccount getDestinationAccount() { return destinationAccount; }
  public BigDecimal getAmount() { return amount; }
  public LocalDate getTransferDate() { return transferDate; }
  public String getDescription() { return description; }
  public UUID getSourceTransactionId() { return sourceTransactionId; }
  public UUID getDestinationTransactionId() { return destinationTransactionId; }
  public UUID getOwnerUserId() { return ownerUserId; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
}

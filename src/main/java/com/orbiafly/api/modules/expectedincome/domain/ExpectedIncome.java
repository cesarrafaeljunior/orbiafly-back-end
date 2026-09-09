package com.orbiafly.api.modules.expectedincome.domain;

import com.orbiafly.api.modules.financialaccount.domain.FinancialAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "expected_income")
public class ExpectedIncome {

  @Id
  private UUID id;

  @Column(nullable = false, length = 160)
  private String description;

  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal amount;

  @Column(name = "expected_date", nullable = false)
  private LocalDate expectedDate;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "financial_account_id")
  private FinancialAccount financialAccount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ExpectedIncomeStatus status;

  @Column(length = 500)
  private String notes;

  @Column(name = "received_at")
  private Instant receivedAt;

  @Column(name = "financial_transaction_id", unique = true)
  private UUID financialTransactionId;

  @Column(name = "owner_user_id", nullable = false)
  private UUID ownerUserId;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected ExpectedIncome() {
  }

  public ExpectedIncome(
      String description,
      BigDecimal amount,
      LocalDate expectedDate,
      FinancialAccount financialAccount,
      String notes,
      UUID ownerUserId,
      Instant now) {
    this.id = UUID.randomUUID();
    this.description = description;
    this.amount = amount;
    this.expectedDate = expectedDate;
    this.financialAccount = financialAccount;
    this.notes = notes;
    this.ownerUserId = ownerUserId;
    this.status = ExpectedIncomeStatus.PENDING;
    this.createdAt = now;
    this.updatedAt = now;
  }

  public void update(
      String description,
      BigDecimal amount,
      LocalDate expectedDate,
      FinancialAccount financialAccount,
      String notes,
      Instant now) {
    if (status != ExpectedIncomeStatus.PENDING) {
      throw new IllegalStateException("Somente recebimentos a receber podem ser editados.");
    }
    this.description = description;
    this.amount = amount;
    this.expectedDate = expectedDate;
    this.financialAccount = financialAccount;
    this.notes = notes;
    this.updatedAt = now;
  }

  public void markReceived(UUID transactionId, Instant now) {
    if (status == ExpectedIncomeStatus.RECEIVED) {
      throw new IllegalStateException("Este recebimento já foi efetivado.");
    }
    if (status == ExpectedIncomeStatus.CANCELED) {
      throw new IllegalStateException("Recebimentos cancelados não podem ser efetivados.");
    }
    this.status = ExpectedIncomeStatus.RECEIVED;
    this.receivedAt = now;
    this.financialTransactionId = transactionId;
    this.updatedAt = now;
  }

  public void cancel(Instant now) {
    if (status == ExpectedIncomeStatus.RECEIVED) {
      throw new IllegalStateException("Recebimentos efetivados não podem ser cancelados neste MVP.");
    }
    if (status != ExpectedIncomeStatus.CANCELED) {
      this.status = ExpectedIncomeStatus.CANCELED;
      this.updatedAt = now;
    }
  }

  public void softDelete(Instant now) {
    this.deletedAt = now;
    this.updatedAt = now;
  }

  public UUID getId() { return id; }
  public String getDescription() { return description; }
  public BigDecimal getAmount() { return amount; }
  public LocalDate getExpectedDate() { return expectedDate; }
  public FinancialAccount getFinancialAccount() { return financialAccount; }
  public ExpectedIncomeStatus getStatus() { return status; }
  public String getNotes() { return notes; }
  public Instant getReceivedAt() { return receivedAt; }
  public UUID getFinancialTransactionId() { return financialTransactionId; }
  public UUID getOwnerUserId() { return ownerUserId; }
  public Instant getDeletedAt() { return deletedAt; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
}

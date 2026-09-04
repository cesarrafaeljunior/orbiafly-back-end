package com.orbiafly.api.modules.loan.domain;

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
@Table(name = "loan_entry")
public class LoanEntry {

  @Id
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "loan_id", nullable = false)
  private Loan loan;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "installment_id")
  private LoanInstallment installment;

  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal amount;

  @Column(name = "occurred_on", nullable = false)
  private LocalDate occurredOn;

  @Column(name = "financial_account_id", nullable = false)
  private UUID financialAccountId;

  @Column(name = "financial_transaction_id", nullable = false)
  private UUID financialTransactionId;

  @Column(name = "owner_user_id", nullable = false)
  private UUID ownerUserId;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected LoanEntry() {
  }

  public LoanEntry(
      Loan loan,
      LoanInstallment installment,
      BigDecimal amount,
      LocalDate occurredOn,
      UUID financialAccountId,
      UUID financialTransactionId,
      UUID ownerUserId,
      Instant now) {
    this.id = UUID.randomUUID();
    this.loan = loan;
    this.installment = installment;
    this.amount = amount;
    this.occurredOn = occurredOn;
    this.financialAccountId = financialAccountId;
    this.financialTransactionId = financialTransactionId;
    this.ownerUserId = ownerUserId;
    this.createdAt = now;
  }

  public void attachFinancialTransaction(UUID financialTransactionId) {
    this.financialTransactionId = financialTransactionId;
  }

  public UUID getId() {
    return id;
  }

  public LoanInstallment getInstallment() {
    return installment;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public LocalDate getOccurredOn() {
    return occurredOn;
  }

  public UUID getFinancialAccountId() {
    return financialAccountId;
  }

  public UUID getFinancialTransactionId() {
    return financialTransactionId;
  }

  public UUID getOwnerUserId() {
    return ownerUserId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}

package com.orbiafly.api.modules.financialaccount.domain;

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
@Table(name = "financial_transaction")
public class FinancialTransaction {

  @Id
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "financial_account_id", nullable = false)
  private FinancialAccount financialAccount;

  @Enumerated(EnumType.STRING)
  @Column(name = "transaction_type", nullable = false, length = 20)
  private FinancialTransactionType type;

  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal amount;

  @Column(nullable = false, length = 180)
  private String description;

  @Column(name = "occurred_on", nullable = false)
  private LocalDate occurredOn;

  @Enumerated(EnumType.STRING)
  @Column(name = "reference_type", length = 40)
  private FinancialReferenceType referenceType;

  @Column(name = "reference_id")
  private UUID referenceId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reversal_of_id")
  private FinancialTransaction reversalOf;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected FinancialTransaction() {
  }

  public FinancialTransaction(
      FinancialAccount financialAccount,
      FinancialTransactionType type,
      BigDecimal amount,
      String description,
      LocalDate occurredOn,
      FinancialReferenceType referenceType,
      UUID referenceId,
      FinancialTransaction reversalOf,
      Instant createdAt) {
    this.id = UUID.randomUUID();
    this.financialAccount = financialAccount;
    this.type = type;
    this.amount = amount;
    this.description = description;
    this.occurredOn = occurredOn;
    this.referenceType = referenceType;
    this.referenceId = referenceId;
    this.reversalOf = reversalOf;
    this.createdAt = createdAt;
  }

  public UUID getId() {
    return id;
  }

  public FinancialAccount getFinancialAccount() {
    return financialAccount;
  }

  public FinancialTransactionType getType() {
    return type;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public String getDescription() {
    return description;
  }

  public LocalDate getOccurredOn() {
    return occurredOn;
  }

  public FinancialReferenceType getReferenceType() {
    return referenceType;
  }

  public UUID getReferenceId() {
    return referenceId;
  }

  public FinancialTransaction getReversalOf() {
    return reversalOf;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}

package com.orbiafly.api.modules.loan.domain;

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
@Table(name = "loan_installment")
public class LoanInstallment {

  @Id
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "loan_id", nullable = false)
  private Loan loan;

  @Column(name = "installment_number", nullable = false)
  private int installmentNumber;

  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal amount;

  @Column(name = "due_date", nullable = false)
  private LocalDate dueDate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private LoanInstallmentStatus status;

  @Column(name = "completed_at")
  private LocalDate completedAt;

  @Column(name = "owner_user_id", nullable = false)
  private UUID ownerUserId;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected LoanInstallment() {
  }

  public LoanInstallment(
      Loan loan,
      int installmentNumber,
      BigDecimal amount,
      LocalDate dueDate,
      UUID ownerUserId,
      Instant now) {
    this.id = UUID.randomUUID();
    this.loan = loan;
    this.installmentNumber = installmentNumber;
    this.amount = amount;
    this.dueDate = dueDate;
    this.status = LoanInstallmentStatus.PENDING;
    this.ownerUserId = ownerUserId;
    this.createdAt = now;
    this.updatedAt = now;
  }

  public void complete(LocalDate completedAt, Instant now) {
    this.status = LoanInstallmentStatus.COMPLETED;
    this.completedAt = completedAt;
    this.updatedAt = now;
  }

  public UUID getId() {
    return id;
  }

  public int getInstallmentNumber() {
    return installmentNumber;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public LocalDate getDueDate() {
    return dueDate;
  }

  public LoanInstallmentStatus getStatus() {
    return status;
  }

  public LocalDate getCompletedAt() {
    return completedAt;
  }

  public UUID getOwnerUserId() {
    return ownerUserId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}

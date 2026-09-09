package com.orbiafly.api.modules.oneoffexpense.domain;

import com.orbiafly.api.modules.fixedexpense.domain.Category;
import com.orbiafly.api.modules.fixedexpense.domain.PaymentStatus;
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
@Table(name = "one_off_expense")
public class OneOffExpense {

  @Id
  private UUID id;

  @Column(name = "owner_user_id", nullable = false)
  private UUID ownerUserId;

  @Column(nullable = false, length = 120)
  private String name;

  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal amount;

  @Column(name = "expense_date", nullable = false)
  private LocalDate expenseDate;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "category_id", nullable = false)
  private Category category;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PaymentStatus status;

  @Column(name = "paid_at")
  private Instant paidAt;

  @Column(name = "financial_account_id")
  private UUID financialAccountId;

  @Column(name = "payment_transaction_id")
  private UUID paymentTransactionId;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected OneOffExpense() {
  }

  public OneOffExpense(
      UUID ownerUserId,
      String name,
      BigDecimal amount,
      LocalDate expenseDate,
      Category category,
      Instant now) {
    this.id = UUID.randomUUID();
    this.ownerUserId = ownerUserId;
    this.name = name;
    this.amount = amount;
    this.expenseDate = expenseDate;
    this.category = category;
    this.status = PaymentStatus.PENDING;
    this.createdAt = now;
    this.updatedAt = now;
  }

  public void updateOneOffExpense(
      String name,
      BigDecimal amount,
      LocalDate expenseDate,
      Category category,
      Instant now) {
    this.name = name;
    this.amount = amount;
    this.expenseDate = expenseDate;
    this.category = category;
    this.updatedAt = now;
  }

  public void updateStatus(PaymentStatus status, Instant now) {
    this.status = status;
    this.paidAt = status == PaymentStatus.PAID ? now : null;
    if (status == PaymentStatus.PENDING) {
      this.financialAccountId = null;
      this.paymentTransactionId = null;
    }
    this.updatedAt = now;
  }

  public void registerPayment(UUID accountId, UUID transactionId, Instant now) {
    this.status = PaymentStatus.PAID;
    this.paidAt = now;
    this.financialAccountId = accountId;
    this.paymentTransactionId = transactionId;
    this.updatedAt = now;
  }

  public void unlinkPayment(Instant now) {
    updateStatus(PaymentStatus.PENDING, now);
  }

  public UUID getId() { return id; }
  public UUID getOwnerUserId() { return ownerUserId; }
  public String getName() { return name; }
  public BigDecimal getAmount() { return amount; }
  public LocalDate getExpenseDate() { return expenseDate; }
  public Category getCategory() { return category; }
  public PaymentStatus getStatus() { return status; }
  public Instant getPaidAt() { return paidAt; }
  public UUID getFinancialAccountId() { return financialAccountId; }
  public UUID getPaymentTransactionId() { return paymentTransactionId; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
}

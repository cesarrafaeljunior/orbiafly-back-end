package com.orbiafly.api.modules.installmentexpense.domain;

import com.orbiafly.api.modules.fixedexpense.domain.PaymentStatus;
import com.orbiafly.api.modules.user.domain.User;
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
@Table(name = "installment_expense_installment")
public class InstallmentExpenseInstallment {

  @Id
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "purchase_id", nullable = false)
  private InstallmentPurchase purchase;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User owner;

  @Column(name = "installment_number", nullable = false)
  private int installmentNumber;

  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal amount;

  @Column(name = "due_date", nullable = false)
  private LocalDate dueDate;

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

  protected InstallmentExpenseInstallment() {
  }

  public InstallmentExpenseInstallment(
      InstallmentPurchase purchase,
      User owner,
      int installmentNumber,
      BigDecimal amount,
      LocalDate dueDate,
      Instant now) {
    this.id = UUID.randomUUID();
    this.purchase = purchase;
    this.owner = owner;
    this.installmentNumber = installmentNumber;
    this.amount = amount;
    this.dueDate = dueDate;
    this.status = PaymentStatus.PENDING;
    this.createdAt = now;
    this.updatedAt = now;
  }

  public void registerPayment(UUID accountId, UUID transactionId, Instant now) {
    this.status = PaymentStatus.PAID;
    this.paidAt = now;
    this.financialAccountId = accountId;
    this.paymentTransactionId = transactionId;
    this.updatedAt = now;
  }

  public UUID getId() { return id; }
  public InstallmentPurchase getPurchase() { return purchase; }
  public User getOwner() { return owner; }
  public int getInstallmentNumber() { return installmentNumber; }
  public BigDecimal getAmount() { return amount; }
  public LocalDate getDueDate() { return dueDate; }
  public PaymentStatus getStatus() { return status; }
  public Instant getPaidAt() { return paidAt; }
  public UUID getFinancialAccountId() { return financialAccountId; }
  public UUID getPaymentTransactionId() { return paymentTransactionId; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
}

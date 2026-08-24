package com.orbiafly.api.modules.fixedexpense.domain;

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
import java.util.UUID;

@Entity
@Table(name = "fixed_expense_occurrence")
public class FixedExpenseOccurrence {

  @Id
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "fixed_expense_id", nullable = false)
  private FixedExpense fixedExpense;

  @Column(name = "occurrence_year", nullable = false)
  private int year;

  @Column(name = "occurrence_month", nullable = false)
  private int month;

  @Column(nullable = false, length = 120)
  private String name;

  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal amount;

  @Column(name = "due_day", nullable = false)
  private int dueDay;

  @Column(name = "category_id", nullable = false)
  private UUID categoryId;

  @Column(name = "category_name", nullable = false, length = 80)
  private String categoryName;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PaymentStatus status;

  @Column(name = "paid_at")
  private Instant paidAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected FixedExpenseOccurrence() {
  }

  public void updateAmount(BigDecimal amount, Instant now) {
    this.amount = amount;
    this.updatedAt = now;
  }

  public void updateStatus(PaymentStatus status, Instant now) {
    this.status = status;
    this.paidAt = status == PaymentStatus.PAID ? now : null;
    this.updatedAt = now;
  }

  public UUID getId() {
    return id;
  }

  public FixedExpense getFixedExpense() {
    return fixedExpense;
  }

  public int getYear() {
    return year;
  }

  public int getMonth() {
    return month;
  }

  public String getName() {
    return name;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public int getDueDay() {
    return dueDay;
  }

  public UUID getCategoryId() {
    return categoryId;
  }

  public String getCategoryName() {
    return categoryName;
  }

  public PaymentStatus getStatus() {
    return status;
  }

  public Instant getPaidAt() {
    return paidAt;
  }
}

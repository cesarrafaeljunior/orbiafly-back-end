package com.orbiafly.api.modules.fixedexpense.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "fixed_expense")
public class FixedExpense {

  @Id
  private UUID id;

  @Column(nullable = false, length = 120)
  private String name;

  @Column(name = "default_amount", nullable = false, precision = 15, scale = 2)
  private BigDecimal defaultAmount;

  @Column(name = "due_day", nullable = false)
  private int dueDay;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "category_id", nullable = false)
  private Category category;

  @Column(nullable = false)
  private boolean active;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected FixedExpense() {
  }

  public FixedExpense(
      String name,
      BigDecimal defaultAmount,
      int dueDay,
      Category category,
      Instant now) {
    this.id = UUID.randomUUID();
    this.name = name;
    this.defaultAmount = defaultAmount;
    this.dueDay = dueDay;
    this.category = category;
    this.active = true;
    this.createdAt = now;
    this.updatedAt = now;
  }

  public void updateFixedExpense(
      String name,
      BigDecimal defaultAmount,
      int dueDay,
      Category category,
      boolean active,
      Instant now) {
    this.name = name;
    this.defaultAmount = defaultAmount;
    this.dueDay = dueDay;
    this.category = category;
    this.active = active;
    this.updatedAt = now;
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public BigDecimal getDefaultAmount() {
    return defaultAmount;
  }

  public int getDueDay() {
    return dueDay;
  }

  public Category getCategory() {
    return category;
  }

  public boolean isActive() {
    return active;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}

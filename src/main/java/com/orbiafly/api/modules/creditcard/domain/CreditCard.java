package com.orbiafly.api.modules.creditcard.domain;

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
import java.util.UUID;

@Entity
@Table(name = "credit_card")
public class CreditCard {

  @Id
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User owner;

  @Column(nullable = false, length = 120)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private CreditCardBrand brand;

  @Column(name = "last_four_digits", length = 4)
  private String lastFourDigits;

  @Column(name = "total_limit", nullable = false, precision = 15, scale = 2)
  private BigDecimal totalLimit;

  @Column(name = "closing_day", nullable = false)
  private int closingDay;

  @Column(name = "due_day", nullable = false)
  private int dueDay;

  @Column(nullable = false)
  private boolean active;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected CreditCard() {
  }

  public CreditCard(
      User owner,
      String name,
      CreditCardBrand brand,
      String lastFourDigits,
      BigDecimal totalLimit,
      int closingDay,
      int dueDay,
      Instant now) {
    this.id = UUID.randomUUID();
    this.owner = owner;
    this.name = name;
    this.brand = brand;
    this.lastFourDigits = lastFourDigits;
    this.totalLimit = totalLimit;
    this.closingDay = closingDay;
    this.dueDay = dueDay;
    this.active = true;
    this.createdAt = now;
    this.updatedAt = now;
  }

  public void updateCreditCard(
      String name,
      CreditCardBrand brand,
      String lastFourDigits,
      BigDecimal totalLimit,
      int closingDay,
      int dueDay,
      Instant now) {
    this.name = name;
    this.brand = brand;
    this.lastFourDigits = lastFourDigits;
    this.totalLimit = totalLimit;
    this.closingDay = closingDay;
    this.dueDay = dueDay;
    this.updatedAt = now;
  }

  public void deactivate(Instant now) {
    this.active = false;
    this.updatedAt = now;
  }

  public void reactivateCreditCard(Instant now) {
    this.active = true;
    this.updatedAt = now;
  }

  public void softDelete(Instant now) {
    this.active = false;
    this.deletedAt = now;
    this.updatedAt = now;
  }

  public UUID getId() {
    return id;
  }

  public User getOwner() {
    return owner;
  }

  public String getName() {
    return name;
  }

  public CreditCardBrand getBrand() {
    return brand;
  }

  public String getLastFourDigits() {
    return lastFourDigits;
  }

  public BigDecimal getTotalLimit() {
    return totalLimit;
  }

  public int getClosingDay() {
    return closingDay;
  }

  public int getDueDay() {
    return dueDay;
  }

  public boolean isActive() {
    return active;
  }

  public Instant getDeletedAt() {
    return deletedAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}

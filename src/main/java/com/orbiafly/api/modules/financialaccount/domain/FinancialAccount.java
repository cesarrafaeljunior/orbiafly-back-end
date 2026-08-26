package com.orbiafly.api.modules.financialaccount.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "financial_account")
public class FinancialAccount {

  @Id
  private UUID id;

  @Column(nullable = false, length = 120)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "account_type", nullable = false, length = 30)
  private FinancialAccountType type;

  @Column(length = 120)
  private String institution;

  @Column(name = "opening_balance", nullable = false, precision = 15, scale = 2)
  private BigDecimal openingBalance;

  @Column(nullable = false)
  private boolean active;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected FinancialAccount() {
  }

  public FinancialAccount(
      String name,
      FinancialAccountType type,
      String institution,
      BigDecimal openingBalance,
      Instant now) {
    this.id = UUID.randomUUID();
    this.name = name;
    this.type = type;
    this.institution = institution;
    this.openingBalance = openingBalance;
    this.active = true;
    this.createdAt = now;
    this.updatedAt = now;
  }

  public void updateFinancialAccount(
      String name,
      FinancialAccountType type,
      String institution,
      Instant now) {
    this.name = name;
    this.type = type;
    this.institution = institution;
    this.updatedAt = now;
  }

  public void deactivate(Instant now) {
    this.active = false;
    this.updatedAt = now;
  }

  public void reactivateFinancialAccount(Instant now) {
    this.active = true;
    this.updatedAt = now;
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public FinancialAccountType getType() {
    return type;
  }

  public String getInstitution() {
    return institution;
  }

  public BigDecimal getOpeningBalance() {
    return openingBalance;
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

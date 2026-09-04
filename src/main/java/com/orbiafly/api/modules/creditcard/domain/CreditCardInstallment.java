package com.orbiafly.api.modules.creditcard.domain;

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
@Table(name = "credit_card_installment")
public class CreditCardInstallment {

  @Id
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "purchase_id", nullable = false)
  private CreditCardPurchase purchase;

  @Column(name = "installment_number", nullable = false)
  private int installmentNumber;

  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal amount;

  @Column(name = "competence_year", nullable = false)
  private int competenceYear;

  @Column(name = "competence_month", nullable = false)
  private int competenceMonth;

  @Column(name = "due_date", nullable = false)
  private LocalDate dueDate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private CreditCardInstallmentStatus status;

  @Column(name = "owner_user_id", nullable = false)
  private UUID ownerUserId;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected CreditCardInstallment() {
  }

  public CreditCardInstallment(
      CreditCardPurchase purchase,
      int installmentNumber,
      BigDecimal amount,
      int competenceYear,
      int competenceMonth,
      LocalDate dueDate,
      UUID ownerUserId,
      Instant now) {
    this.id = UUID.randomUUID();
    this.purchase = purchase;
    this.installmentNumber = installmentNumber;
    this.amount = amount;
    this.competenceYear = competenceYear;
    this.competenceMonth = competenceMonth;
    this.dueDate = dueDate;
    this.status = CreditCardInstallmentStatus.PENDING;
    this.ownerUserId = ownerUserId;
    this.createdAt = now;
  }

  public UUID getId() {
    return id;
  }

  public CreditCardPurchase getPurchase() {
    return purchase;
  }

  public int getInstallmentNumber() {
    return installmentNumber;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public int getCompetenceYear() {
    return competenceYear;
  }

  public int getCompetenceMonth() {
    return competenceMonth;
  }

  public LocalDate getDueDate() {
    return dueDate;
  }

  public CreditCardInstallmentStatus getStatus() {
    return status;
  }

  public UUID getOwnerUserId() {
    return ownerUserId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}

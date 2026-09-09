package com.orbiafly.api.modules.creditcard.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "credit_card_purchase")
public class CreditCardPurchase {

  @Id
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "credit_card_id", nullable = false)
  private CreditCard creditCard;

  @Column(nullable = false, length = 160)
  private String description;

  @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
  private BigDecimal totalAmount;

  @Column(name = "purchase_date", nullable = false)
  private LocalDate purchaseDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "purchase_type", nullable = false, length = 30)
  private CreditCardPurchaseType purchaseType;

  @Column(name = "installment_count", nullable = false)
  private int installmentCount;

  @Column(name = "installment_amount", nullable = false, precision = 15, scale = 2)
  private BigDecimal installmentAmount;

  @Column(name = "owner_user_id", nullable = false)
  private UUID ownerUserId;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<CreditCardInstallment> installments = new ArrayList<>();

  protected CreditCardPurchase() {
  }

  public CreditCardPurchase(
      CreditCard creditCard,
      String description,
      BigDecimal totalAmount,
      LocalDate purchaseDate,
      CreditCardPurchaseType purchaseType,
      int installmentCount,
      BigDecimal installmentAmount,
      UUID ownerUserId,
      Instant now) {
    this.id = UUID.randomUUID();
    this.creditCard = creditCard;
    this.description = description;
    this.totalAmount = totalAmount;
    this.purchaseDate = purchaseDate;
    this.purchaseType = purchaseType;
    this.installmentCount = installmentCount;
    this.installmentAmount = installmentAmount;
    this.ownerUserId = ownerUserId;
    this.createdAt = now;
    this.updatedAt = now;
  }

  public void addInstallment(CreditCardInstallment installment) {
    installments.add(installment);
  }

  public void softDelete(Instant now) {
    this.deletedAt = now;
    this.updatedAt = now;
  }

  public void updateDetails(
      CreditCard creditCard,
      String description,
      BigDecimal totalAmount,
      LocalDate purchaseDate,
      CreditCardPurchaseType purchaseType,
      int installmentCount,
      BigDecimal installmentAmount,
      Instant now) {
    this.creditCard = creditCard;
    this.description = description;
    this.totalAmount = totalAmount;
    this.purchaseDate = purchaseDate;
    this.purchaseType = purchaseType;
    this.installmentCount = installmentCount;
    this.installmentAmount = installmentAmount;
    this.updatedAt = now;
  }

  public void replaceInstallments(List<CreditCardInstallment> installments, Instant now) {
    this.installments.clear();
    this.installments.addAll(installments);
    this.updatedAt = now;
  }

  public void refreshTotals(Instant now) {
    this.totalAmount = installments.stream()
        .map(CreditCardInstallment::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    this.installmentAmount = this.totalAmount
        .divide(BigDecimal.valueOf(installmentCount), 2, RoundingMode.HALF_UP);
    this.updatedAt = now;
  }

  public UUID getId() {
    return id;
  }

  public CreditCard getCreditCard() {
    return creditCard;
  }

  public String getDescription() {
    return description;
  }

  public BigDecimal getTotalAmount() {
    return totalAmount;
  }

  public LocalDate getPurchaseDate() {
    return purchaseDate;
  }

  public CreditCardPurchaseType getPurchaseType() {
    return purchaseType;
  }

  public int getInstallmentCount() {
    return installmentCount;
  }

  public BigDecimal getInstallmentAmount() {
    return installmentAmount;
  }

  public UUID getOwnerUserId() {
    return ownerUserId;
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

  public List<CreditCardInstallment> getInstallments() {
    return installments;
  }
}

package com.orbiafly.api.modules.installmentexpense.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "installment_purchase")
public class InstallmentPurchase {

  @Id
  private UUID id;

  @Column(name = "owner_user_id", nullable = false)
  private UUID ownerUserId;

  @Column(nullable = false, length = 180)
  private String description;

  @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
  private BigDecimal totalAmount;

  @Column(name = "installment_count", nullable = false)
  private int installmentCount;

  @Column(name = "first_installment_date", nullable = false)
  private LocalDate firstInstallmentDate;

  @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("installmentNumber ASC")
  private List<InstallmentExpenseInstallment> installments = new ArrayList<>();

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected InstallmentPurchase() {
  }

  public InstallmentPurchase(
      UUID ownerUserId,
      String description,
      BigDecimal totalAmount,
      int installmentCount,
      LocalDate firstInstallmentDate,
      Instant now) {
    this.id = UUID.randomUUID();
    this.ownerUserId = ownerUserId;
    this.description = description;
    this.totalAmount = totalAmount;
    this.installmentCount = installmentCount;
    this.firstInstallmentDate = firstInstallmentDate;
    this.createdAt = now;
    this.updatedAt = now;
  }

  public void addInstallment(InstallmentExpenseInstallment installment) {
    installments.add(installment);
  }

  public UUID getId() { return id; }
  public UUID getOwnerUserId() { return ownerUserId; }
  public String getDescription() { return description; }
  public BigDecimal getTotalAmount() { return totalAmount; }
  public int getInstallmentCount() { return installmentCount; }
  public LocalDate getFirstInstallmentDate() { return firstInstallmentDate; }
  public List<InstallmentExpenseInstallment> getInstallments() { return installments; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
}

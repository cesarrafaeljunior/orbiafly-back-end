package com.orbiafly.api.modules.loan.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "loan")
public class Loan {

  @Id
  private UUID id;

  @Column(nullable = false, length = 140)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private LoanDirection direction;

  @Column(nullable = false, length = 140)
  private String counterparty;

  @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
  private BigDecimal totalAmount;

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @Column(name = "installment_count")
  private Integer installmentCount;

  @Column(name = "installment_amount", precision = 15, scale = 2)
  private BigDecimal installmentAmount;

  @Column(name = "first_due_date")
  private LocalDate firstDueDate;

  @Column(name = "interest_rate", length = 60)
  private String interestRate;

  @Column(length = 500)
  private String notes;

  @Column(name = "owner_user_id", nullable = false)
  private UUID ownerUserId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private LoanStatus status;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<LoanInstallment> installments = new LinkedHashSet<>();

  @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<LoanEntry> entries = new LinkedHashSet<>();

  protected Loan() {
  }

  public Loan(
      String name,
      LoanDirection direction,
      String counterparty,
      BigDecimal totalAmount,
      LocalDate startDate,
      Integer installmentCount,
      BigDecimal installmentAmount,
      LocalDate firstDueDate,
      String interestRate,
      String notes,
      UUID ownerUserId,
      Instant now) {
    this.id = UUID.randomUUID();
    this.name = name;
    this.direction = direction;
    this.counterparty = counterparty;
    this.totalAmount = totalAmount;
    this.startDate = startDate;
    this.installmentCount = installmentCount;
    this.installmentAmount = installmentAmount;
    this.firstDueDate = firstDueDate;
    this.interestRate = interestRate;
    this.notes = notes;
    this.ownerUserId = ownerUserId;
    this.status = LoanStatus.ACTIVE;
    this.createdAt = now;
    this.updatedAt = now;
  }

  public void addInstallment(LoanInstallment installment) {
    installments.add(installment);
  }

  public void addEntry(LoanEntry entry) {
    entries.add(entry);
  }

  public LoanInstallment nextPendingInstallment() {
    return installments.stream()
        .filter(installment -> installment.getStatus() == LoanInstallmentStatus.PENDING)
        .min(Comparator.comparingInt(LoanInstallment::getInstallmentNumber))
        .orElse(null);
  }

  public BigDecimal totalSettled() {
    return entries.stream()
        .map(LoanEntry::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public BigDecimal remainingAmount() {
    BigDecimal remaining = totalAmount.subtract(totalSettled());
    return remaining.signum() < 0 ? BigDecimal.ZERO : remaining;
  }

  public void refreshStatus(Instant now) {
    this.status = remainingAmount().signum() == 0 ? LoanStatus.COMPLETED : LoanStatus.ACTIVE;
    this.updatedAt = now;
  }

  public void softDelete(Instant now) {
    this.deletedAt = now;
    this.updatedAt = now;
  }

  public boolean hasInstallments() {
    return installmentCount != null;
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public LoanDirection getDirection() {
    return direction;
  }

  public String getCounterparty() {
    return counterparty;
  }

  public BigDecimal getTotalAmount() {
    return totalAmount;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public Integer getInstallmentCount() {
    return installmentCount;
  }

  public BigDecimal getInstallmentAmount() {
    return installmentAmount;
  }

  public LocalDate getFirstDueDate() {
    return firstDueDate;
  }

  public String getInterestRate() {
    return interestRate;
  }

  public String getNotes() {
    return notes;
  }

  public UUID getOwnerUserId() {
    return ownerUserId;
  }

  public LoanStatus getStatus() {
    return status;
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

  public Set<LoanInstallment> getInstallments() {
    return installments;
  }

  public Set<LoanEntry> getEntries() {
    return entries;
  }
}

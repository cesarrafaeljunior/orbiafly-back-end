package com.orbiafly.api.modules.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "password_reset_code")
public class PasswordResetCode {

  @Id
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private AppUser user;

  @Column(name = "code_hash", nullable = false, length = 120)
  private String codeHash;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "used_at")
  private Instant usedAt;

  @Column(name = "failed_attempts", nullable = false)
  private int failedAttempts;

  protected PasswordResetCode() {
  }

  public PasswordResetCode(AppUser user, String codeHash, Instant createdAt, Instant expiresAt) {
    this.id = UUID.randomUUID();
    this.user = user;
    this.codeHash = codeHash;
    this.createdAt = createdAt;
    this.expiresAt = expiresAt;
  }

  public void markUsed(Instant now) {
    this.usedAt = now;
  }

  public void registerFailedAttempt(Instant now, int maximumAttempts) {
    failedAttempts++;
    if (failedAttempts >= maximumAttempts) {
      markUsed(now);
    }
  }

  public UUID getId() {
    return id;
  }

  public AppUser getUser() {
    return user;
  }

  public String getCodeHash() {
    return codeHash;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public Instant getUsedAt() {
    return usedAt;
  }

  public int getFailedAttempts() {
    return failedAttempts;
  }
}

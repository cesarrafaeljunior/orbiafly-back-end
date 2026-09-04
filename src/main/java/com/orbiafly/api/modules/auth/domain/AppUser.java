package com.orbiafly.api.modules.auth.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "app_user")
public class AppUser {

  @Id
  private UUID id;

  @Column(nullable = false, unique = true, length = 160)
  private String email;

  @Column(nullable = false, unique = true, length = 80)
  private String username;

  @Column(name = "password_hash", nullable = false, length = 120)
  private String passwordHash;

  @Column(nullable = false)
  private boolean active;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "app_user_role", joinColumns = @JoinColumn(name = "user_id"))
  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false, length = 30)
  private Set<AppRole> roles = new LinkedHashSet<>();

  protected AppUser() {
  }

  public AppUser(
      String email,
      String username,
      String passwordHash,
      Set<AppRole> roles,
      Instant now) {
    this.id = UUID.randomUUID();
    this.email = email;
    this.username = username;
    this.passwordHash = passwordHash;
    this.active = true;
    this.createdAt = now;
    this.updatedAt = now;
    this.roles.addAll(roles);
  }

  public void updatePasswordHash(String passwordHash, Instant now) {
    this.passwordHash = passwordHash;
    this.updatedAt = now;
  }

  public UUID getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public String getUsername() {
    return username;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public boolean isActive() {
    return active;
  }

  public Set<AppRole> getRoles() {
    return roles;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}

package com.orbiafly.api.modules.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "app_user")
public class User {

  @Id
  private UUID id;

  @Column(nullable = false, unique = true, length = 80)
  private String username;

  @Column(nullable = false, unique = true, length = 160)
  private String email;

  @Column(nullable = false, length = 120)
  private String password;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private UserRole role;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected User() {
  }

  public User(String username, String email, String password, UserRole role, Instant now) {
    this.id = UUID.randomUUID();
    this.username = username;
    this.email = email;
    this.password = password;
    this.role = role;
    this.createdAt = now;
    this.updatedAt = now;
  }

  public void updatePassword(String password, Instant now) {
    this.password = password;
    this.updatedAt = now;
  }

  public UUID getId() { return id; }
  public String getUsername() { return username; }
  public String getEmail() { return email; }
  public String getPassword() { return password; }
  public UserRole getRole() { return role; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
}

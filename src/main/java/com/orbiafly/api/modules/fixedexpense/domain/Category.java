package com.orbiafly.api.modules.fixedexpense.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "category")
public class Category {

  @Id
  private UUID id;

  @Column(nullable = false, length = 80)
  private String name;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected Category() {
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }
}

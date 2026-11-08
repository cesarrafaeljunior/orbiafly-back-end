package com.orbiafly.api.infrastructure.security;

import com.orbiafly.api.modules.auth.domain.AppRole;
import com.orbiafly.api.modules.auth.domain.AppUser;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class AppUserPrincipal implements UserDetails {

  private final UUID id;
  private final String email;
  private final String username;
  private final String passwordHash;
  private final boolean active;
  private final long tokenVersion;
  private final Set<AppRole> roles;

  public AppUserPrincipal(AppUser user) {
    this.id = user.getId();
    this.email = user.getEmail();
    this.username = user.getUsername();
    this.passwordHash = user.getPasswordHash();
    this.active = user.isActive();
    this.tokenVersion = user.getTokenVersion();
    this.roles = user.getRoles();
  }

  public UUID id() {
    return id;
  }

  public String email() {
    return email;
  }

  public Set<AppRole> roles() {
    return roles;
  }

  public long tokenVersion() {
    return tokenVersion;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return roles.stream()
        .map(role -> new SimpleGrantedAuthority(role.name()))
        .toList();
  }

  @Override
  public String getPassword() {
    return passwordHash;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public boolean isEnabled() {
    return active;
  }
}

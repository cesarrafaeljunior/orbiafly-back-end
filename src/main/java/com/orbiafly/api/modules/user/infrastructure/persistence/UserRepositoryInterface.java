package com.orbiafly.api.modules.user.infrastructure.persistence;

import com.orbiafly.api.modules.user.domain.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepositoryInterface extends JpaRepository<User, UUID> {

  Optional<User> findByEmailIgnoreCase(String email);
  Optional<User> findByUsernameIgnoreCase(String username);
  boolean existsByEmailIgnoreCase(String email);
  boolean existsByUsernameIgnoreCase(String username);
}

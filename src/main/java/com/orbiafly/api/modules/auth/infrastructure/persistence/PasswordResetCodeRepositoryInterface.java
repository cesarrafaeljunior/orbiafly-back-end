package com.orbiafly.api.modules.auth.infrastructure.persistence;

import com.orbiafly.api.modules.auth.domain.PasswordResetCode;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetCodeRepositoryInterface extends JpaRepository<PasswordResetCode, UUID> {

  List<PasswordResetCode> findByUserIdAndUsedAtIsNullOrderByCreatedAtDesc(UUID userId);
}

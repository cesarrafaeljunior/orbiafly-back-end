package com.orbiafly.api.modules.auth.application.model;

import com.orbiafly.api.modules.user.domain.UserRole;
import java.time.Instant;
import java.util.UUID;

public record AuthenticatedUserResult(
    UUID id,
    String username,
    String email,
    UserRole role,
    Instant createdAt,
    Instant updatedAt) {
}

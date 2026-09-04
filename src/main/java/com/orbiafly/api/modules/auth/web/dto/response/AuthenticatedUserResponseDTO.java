package com.orbiafly.api.modules.auth.web.dto.response;

import com.orbiafly.api.modules.user.domain.UserRole;
import java.time.Instant;
import java.util.UUID;

public record AuthenticatedUserResponseDTO(
    UUID id,
    String username,
    String email,
    UserRole role,
    Instant createdAt,
    Instant updatedAt) {
}

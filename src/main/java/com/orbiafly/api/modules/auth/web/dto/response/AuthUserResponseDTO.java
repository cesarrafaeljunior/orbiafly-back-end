package com.orbiafly.api.modules.auth.web.dto.response;

import java.util.Set;
import java.util.UUID;

public record AuthUserResponseDTO(
    UUID id,
    String email,
    String username,
    Set<String> roles) {
}

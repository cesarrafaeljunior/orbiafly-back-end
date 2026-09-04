package com.orbiafly.api.modules.auth.web.dto.response;

public record AuthResponseDTO(
    String token,
    String tokenType,
    long expiresInSeconds,
    AuthenticatedUserResponseDTO user) {
}

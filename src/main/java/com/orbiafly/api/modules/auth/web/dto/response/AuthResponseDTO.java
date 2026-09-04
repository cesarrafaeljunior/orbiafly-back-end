package com.orbiafly.api.modules.auth.web.dto.response;

public record AuthResponseDTO(
    String accessToken,
    AuthUserResponseDTO user) {
}

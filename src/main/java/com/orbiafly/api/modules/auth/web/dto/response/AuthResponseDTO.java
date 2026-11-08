package com.orbiafly.api.modules.auth.web.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record AuthResponseDTO(
    @JsonIgnore String accessToken,
    AuthUserResponseDTO user) {
}

package com.orbiafly.api.modules.auth.application.model;

public record AuthResult(
    String accessToken,
    AuthUserResult user) {
}

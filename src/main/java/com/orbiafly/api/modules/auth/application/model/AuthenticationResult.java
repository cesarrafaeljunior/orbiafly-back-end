package com.orbiafly.api.modules.auth.application.model;

public record AuthenticationResult(
    String token,
    String tokenType,
    long expiresInSeconds,
    AuthenticatedUserResult user) {
}

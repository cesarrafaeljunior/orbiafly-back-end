package com.orbiafly.api.modules.auth.application.model;

import com.orbiafly.api.modules.auth.domain.AppRole;
import java.util.Set;
import java.util.UUID;

public record AuthUserResult(
    UUID id,
    String email,
    String username,
    Set<AppRole> roles) {
}

package com.momentory.auth.security;

import com.momentory.user.domain.UserRole;

public record LoginPrincipal(
        Long userId,
        UserRole role
) {
}

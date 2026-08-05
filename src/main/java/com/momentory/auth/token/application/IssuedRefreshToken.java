package com.momentory.auth.token.application;

import java.time.Instant;

public record IssuedRefreshToken(
        String value,
        String hash,
        Instant expiresAt
) {
}

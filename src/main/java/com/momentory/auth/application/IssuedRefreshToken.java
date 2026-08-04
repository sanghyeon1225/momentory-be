package com.momentory.auth.application;

import java.time.Instant;

public record IssuedRefreshToken(
        String value,
        String hash,
        Instant expiresAt
) {
}

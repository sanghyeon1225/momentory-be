package com.momentory.auth.token.application;

import java.time.Duration;

public record IssuedAccessToken(
        String value,
        Duration expiresIn
) {
}

package com.momentory.auth.application;

import java.time.Duration;

public record IssuedAccessToken(
        String value,
        Duration expiresIn
) {
}

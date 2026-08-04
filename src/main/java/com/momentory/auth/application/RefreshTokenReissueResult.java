package com.momentory.auth.application;

import java.time.Duration;

public record RefreshTokenReissueResult(
        String accessToken,
        String refreshToken,
        Duration accessTokenExpiresIn
) {
}

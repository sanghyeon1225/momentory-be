package com.momentory.auth.token.application;

import java.time.Duration;

public record RefreshTokenReissueResult(
        String accessToken,
        String refreshToken,
        Duration accessTokenExpiresIn
) {
}

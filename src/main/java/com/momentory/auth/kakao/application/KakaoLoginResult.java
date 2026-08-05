package com.momentory.auth.kakao.application;

import java.time.Duration;

public record KakaoLoginResult(
        String accessToken,
        String refreshToken,
        Duration accessTokenExpiresIn,
        Long userId,
        boolean onboardingRequired
) {
}

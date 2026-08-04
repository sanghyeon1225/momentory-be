package com.momentory.auth.application;

public record KakaoUserInfo(
        String providerUserId,
        String email
) {
}

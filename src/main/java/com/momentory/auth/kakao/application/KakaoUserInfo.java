package com.momentory.auth.kakao.application;

public record KakaoUserInfo(
        String providerUserId,
        String email
) {
}

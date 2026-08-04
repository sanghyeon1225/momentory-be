package com.momentory.auth.infrastructure.kakao;

record KakaoUserResponse(
        Long id,
        KakaoAccount kakao_account
) {

    record KakaoAccount(String email) {
    }
}

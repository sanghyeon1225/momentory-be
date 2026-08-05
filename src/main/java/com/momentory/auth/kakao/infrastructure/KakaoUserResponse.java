package com.momentory.auth.kakao.infrastructure;

record KakaoUserResponse(
        Long id,
        KakaoAccount kakao_account
) {

    record KakaoAccount(String email) {
    }
}

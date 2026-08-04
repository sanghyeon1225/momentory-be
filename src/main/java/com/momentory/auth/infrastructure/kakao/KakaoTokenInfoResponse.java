package com.momentory.auth.infrastructure.kakao;

record KakaoTokenInfoResponse(
        Long id,
        Long app_id,
        Long expires_in
) {
}

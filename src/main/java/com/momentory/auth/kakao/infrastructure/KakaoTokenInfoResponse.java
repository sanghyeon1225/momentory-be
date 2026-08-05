package com.momentory.auth.kakao.infrastructure;

record KakaoTokenInfoResponse(
        Long id,
        Long app_id,
        Long expires_in
) {
}

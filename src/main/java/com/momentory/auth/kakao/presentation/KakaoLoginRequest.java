package com.momentory.auth.kakao.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record KakaoLoginRequest(
        @Schema(description = "React Native 카카오 Native SDK에서 발급한 Access Token")
        @NotBlank(message = "accessToken은 필수입니다.")
        String accessToken
) {
}

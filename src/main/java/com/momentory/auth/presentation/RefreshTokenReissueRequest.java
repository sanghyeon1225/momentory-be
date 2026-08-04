package com.momentory.auth.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record RefreshTokenReissueRequest(
        @Schema(description = "모멘토리 로그인 시 발급한 Refresh Token")
        @NotBlank(message = "refreshToken은 필수입니다.")
        String refreshToken
) {
}

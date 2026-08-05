package com.momentory.auth.presentation;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthErrorResponse(
        @Schema(description = "애플리케이션 오류 코드", example = "AUTHENTICATION_REQUIRED")
        String code,
        @Schema(description = "사용자에게 전달되는 오류 설명", example = "인증이 필요합니다.")
        String message
) {
}

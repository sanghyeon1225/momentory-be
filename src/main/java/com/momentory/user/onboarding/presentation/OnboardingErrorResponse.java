package com.momentory.user.onboarding.presentation;

import io.swagger.v3.oas.annotations.media.Schema;

public record OnboardingErrorResponse(
        @Schema(description = "애플리케이션 오류 코드", example = "INVALID_REQUEST")
        String code,
        @Schema(description = "사용자에게 전달되는 오류 설명", example = "잘못된 요청입니다.")
        String message
) {
}

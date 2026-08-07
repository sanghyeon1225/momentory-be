package com.momentory.common.presentation;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "API 오류 응답")
public record ApiErrorResponse(
        @Schema(description = "클라이언트가 오류 종류를 식별하는 애플리케이션 오류 코드", example = "INVALID_REQUEST")
        String code,
        @Schema(description = "사용자에게 전달되는 오류 설명", example = "잘못된 요청입니다.")
        String message
) {
}

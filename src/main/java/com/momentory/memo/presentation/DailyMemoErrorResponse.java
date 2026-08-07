package com.momentory.memo.presentation;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "하루 메모 API 오류 응답")
public record DailyMemoErrorResponse(
        @Schema(example = "INVALID_REQUEST") String code,
        @Schema(example = "Invalid request.") String message
) {
}

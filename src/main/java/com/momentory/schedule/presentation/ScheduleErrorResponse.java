package com.momentory.schedule.presentation;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "일정 API 오류 응답")
public record ScheduleErrorResponse(
        @Schema(example = "INVALID_REQUEST") String code,
        @Schema(example = "잘못된 요청입니다.") String message
) {
}

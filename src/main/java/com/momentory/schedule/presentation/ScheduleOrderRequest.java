package com.momentory.schedule.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record ScheduleOrderRequest(
        @Schema(example = "2026-08-10")
        @NotNull(message = "날짜는 필수입니다.")
        LocalDate date,
        @Schema(example = "[4, 2, 8, 1]")
        @NotEmpty(message = "일정 목록은 비어 있을 수 없습니다.")
        List<@NotNull(message = "일정 ID는 필수입니다.") Long> scheduleIds
) {
}

package com.momentory.schedule.presentation;

import com.momentory.schedule.domain.Schedule;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ScheduleRequest(
        @Schema(description = "일정 날짜", example = "2026-08-10")
        @NotNull(message = "date는 필수입니다.")
        LocalDate date,
        @Schema(description = "일정 제목", example = "운동하기")
        @NotBlank(message = "title은 필수입니다.")
        @Size(max = Schedule.TITLE_MAX_LENGTH, message = "title은 최대 255자입니다.")
        String title
) {

    public ScheduleRequest {
        title = title == null ? null : title.strip();
    }
}

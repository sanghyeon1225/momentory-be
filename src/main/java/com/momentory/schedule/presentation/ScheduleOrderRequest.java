package com.momentory.schedule.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record ScheduleOrderRequest(
        @Schema(example = "2026-08-10")
        @NotNull(message = "date is required")
        LocalDate date,
        @Schema(example = "[4, 2, 8, 1]")
        @NotEmpty(message = "scheduleIds must not be empty")
        List<@NotNull(message = "scheduleId must not be null") Long> scheduleIds
) {
}

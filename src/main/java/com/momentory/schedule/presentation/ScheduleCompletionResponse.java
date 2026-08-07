package com.momentory.schedule.presentation;

import com.momentory.schedule.application.ScheduleCompletionResult;
import com.momentory.schedule.domain.ScheduleEmotion;
import io.swagger.v3.oas.annotations.media.Schema;

public record ScheduleCompletionResponse(
        @Schema(example = "1") Long scheduleId,
        @Schema(example = "true") boolean completed,
        @Schema(nullable = true, example = "PROUD") ScheduleEmotion emotion
) {

    static ScheduleCompletionResponse from(ScheduleCompletionResult result) {
        return new ScheduleCompletionResponse(result.scheduleId(), result.completed(), result.emotion());
    }
}

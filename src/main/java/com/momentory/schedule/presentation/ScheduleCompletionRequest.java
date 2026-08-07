package com.momentory.schedule.presentation;

import com.momentory.schedule.domain.ScheduleEmotion;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public record ScheduleCompletionRequest(
        @Schema(example = "true")
        @NotNull(message = "completed is required")
        Boolean completed,
        @Schema(nullable = true, example = "PROUD")
        ScheduleEmotion emotion
) {

    @AssertTrue(message = "emotion must be null when completed is false")
    public boolean isCompletionEmotionValid() {
        return completed == null || completed || emotion == null;
    }
}

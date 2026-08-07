package com.momentory.schedule.presentation;

import com.momentory.schedule.domain.ScheduleEmotion;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public record ScheduleCompletionRequest(
        @Schema(example = "true")
        @NotNull(message = "완료 여부를 입력해주세요.")
        Boolean completed,
        @Schema(nullable = true, example = "PROUD")
        ScheduleEmotion emotion
) {

    @JsonIgnore
    @Schema(hidden = true)
    @AssertTrue(message = "미완료 상태에서는 감정을 선택할 수 없습니다.")
    public boolean isCompletionEmotionValid() {
        return completed == null || completed || emotion == null;
    }
}

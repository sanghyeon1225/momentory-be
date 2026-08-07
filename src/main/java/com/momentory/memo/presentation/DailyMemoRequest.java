package com.momentory.memo.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record DailyMemoRequest(
        @Schema(description = "하루 메모 내용", example = "오늘 회의가 많았지만 잘 마무리했다.")
        @NotBlank(message = "content is required.")
        String content
) {

    public DailyMemoRequest {
        content = content == null ? null : content.strip();
    }
}

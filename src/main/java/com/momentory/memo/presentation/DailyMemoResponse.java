package com.momentory.memo.presentation;

import com.momentory.memo.application.DailyMemoResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record DailyMemoResponse(
        @Schema(example = "2026-08-07") LocalDate date,
        @Schema(example = "오늘 회의가 많았지만 잘 마무리했다.") String content
) {

    static DailyMemoResponse from(DailyMemoResult result) {
        return new DailyMemoResponse(result.date(), result.content());
    }
}

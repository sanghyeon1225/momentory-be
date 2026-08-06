package com.momentory.schedule.presentation;

import com.momentory.schedule.application.ScheduleResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record ScheduleListResponse(
        @Schema(description = "일정 목록") List<ScheduleResponse> schedules
) {

    static ScheduleListResponse from(List<ScheduleResult> results) {
        return new ScheduleListResponse(results.stream().map(ScheduleResponse::from).toList());
    }
}

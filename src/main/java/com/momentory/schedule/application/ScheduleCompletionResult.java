package com.momentory.schedule.application;

import com.momentory.schedule.domain.Schedule;
import com.momentory.schedule.domain.ScheduleEmotion;

public record ScheduleCompletionResult(
        Long scheduleId,
        boolean completed,
        ScheduleEmotion emotion
) {

    public static ScheduleCompletionResult from(Schedule schedule) {
        return new ScheduleCompletionResult(schedule.getId(), schedule.isCompleted(), schedule.getEmotion());
    }
}

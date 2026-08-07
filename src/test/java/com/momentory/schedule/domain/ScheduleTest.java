package com.momentory.schedule.domain;

import com.momentory.user.domain.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScheduleTest {

    @Test
    void createsAndUpdatesManualScheduleWithTrimmedTitle() {
        Schedule schedule = Schedule.createManual(User.create(), LocalDate.of(2026, 8, 10), " 운동하기 ", 0L);

        schedule.update(LocalDate.of(2026, 8, 11), " 저녁 운동하기 ");

        assertThat(schedule.getScheduleDate()).isEqualTo(LocalDate.of(2026, 8, 11));
        assertThat(schedule.getTitle()).isEqualTo("저녁 운동하기");
        assertThat(schedule.isCompleted()).isFalse();
        assertThat(schedule.getEmotion()).isNull();
    }

    @Test
    void rejectsBlankAndTooLongTitles() {
        assertThatThrownBy(() -> Schedule.createManual(User.create(), LocalDate.now(), "   ", 0L))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Schedule.createManual(User.create(), LocalDate.now(), "a".repeat(256), 0L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void recordsDeletionOnlyOnce() {
        Schedule schedule = Schedule.createManual(User.create(), LocalDate.now(), "운동하기", 0L);
        Instant firstDeletedAt = Instant.parse("2026-08-10T00:00:00Z");

        schedule.delete(firstDeletedAt);
        schedule.delete(firstDeletedAt.plusSeconds(1));

        assertThat(schedule.isDeleted()).isTrue();
        assertThat(schedule.getDeletedAt()).isEqualTo(firstDeletedAt);
    }

    @Test
    void changesCompletionAndClearsEmotionWhenCompletionIsCancelled() {
        Schedule schedule = Schedule.createManual(User.create(), LocalDate.now(), "운동하기", 0L);

        schedule.changeCompletion(true, ScheduleEmotion.PROUD);

        assertThat(schedule.isCompleted()).isTrue();
        assertThat(schedule.getEmotion()).isEqualTo(ScheduleEmotion.PROUD);

        schedule.changeCompletion(false, null);

        assertThat(schedule.isCompleted()).isFalse();
        assertThat(schedule.getEmotion()).isNull();
    }

    @Test
    void rejectsEmotionForIncompleteSchedule() {
        Schedule schedule = Schedule.createManual(User.create(), LocalDate.now(), "운동하기", 0L);

        assertThatThrownBy(() -> schedule.changeCompletion(false, ScheduleEmotion.PROUD))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

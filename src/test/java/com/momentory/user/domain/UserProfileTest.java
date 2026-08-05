package com.momentory.user.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserProfileTest {

    @Test
    void createsProfileWithServerDefaultTimeZone() {
        UserProfile profile = UserProfile.create(
                User.create(),
                " 모리 ",
                null,
                Gender.UNSPECIFIED,
                Set.of(InterestArea.SELF),
                LocalTime.of(21, 30),
                true
        );

        assertThat(profile.getNickname()).isEqualTo("모리");
        assertThat(profile.getAge()).isNull();
        assertThat(profile.getTimeZone()).isEqualTo("Asia/Seoul");
        assertThat(profile.isCalendarIntegrationEnabled()).isTrue();
    }

    @Test
    void replacesAllProfileFieldsAndInterestAreas() {
        UserProfile profile = UserProfile.create(
                User.create(),
                "모리",
                25,
                Gender.FEMALE,
                Set.of(InterestArea.CAREER, InterestArea.SELF),
                LocalTime.of(21, 30),
                true
        );

        profile.update(
                "새모리",
                30,
                Gender.MALE,
                Set.of(InterestArea.HEALTH),
                LocalTime.of(9, 0),
                false
        );

        assertThat(profile.getNickname()).isEqualTo("새모리");
        assertThat(profile.getAge()).isEqualTo(30);
        assertThat(profile.getGender()).isEqualTo(Gender.MALE);
        assertThat(profile.getInterestAreas()).containsExactly(InterestArea.HEALTH);
        assertThat(profile.getReflectionTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(profile.isCalendarIntegrationEnabled()).isFalse();
    }
}

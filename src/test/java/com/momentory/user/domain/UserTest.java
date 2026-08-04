package com.momentory.user.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void newUserRequiresOnboardingByDefault() {
        User user = User.create();

        assertThat(user.requiresOnboarding()).isTrue();
        assertThat(user.getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    void completeOnboardingMakesOnboardingNotRequired() {
        User user = User.create();

        user.completeOnboarding();

        assertThat(user.requiresOnboarding()).isFalse();
    }
}

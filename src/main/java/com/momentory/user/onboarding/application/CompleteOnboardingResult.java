package com.momentory.user.onboarding.application;

public record CompleteOnboardingResult(
        Long userId,
        boolean onboardingRequired
) {
}

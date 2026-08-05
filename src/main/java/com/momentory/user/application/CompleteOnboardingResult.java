package com.momentory.user.application;

public record CompleteOnboardingResult(
        Long userId,
        boolean onboardingRequired
) {
}

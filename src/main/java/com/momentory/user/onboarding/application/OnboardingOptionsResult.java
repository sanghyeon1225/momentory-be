package com.momentory.user.onboarding.application;

import java.util.List;

public record OnboardingOptionsResult(
        NicknamePolicyResult nickname,
        List<OnboardingOptionResult> genders,
        List<OnboardingOptionResult> interestAreas,
        String reflectionTimeFormat,
        String defaultTimeZone
) {
}

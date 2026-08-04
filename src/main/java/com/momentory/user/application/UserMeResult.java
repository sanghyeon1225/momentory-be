package com.momentory.user.application;

import com.momentory.user.domain.UserRole;

public record UserMeResult(
        Long userId,
        UserRole role,
        boolean onboardingRequired
) {
}

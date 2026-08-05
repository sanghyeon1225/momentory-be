package com.momentory.user.onboarding.application;

public record NicknamePolicyResult(
        int maxLength,
        boolean duplicateAllowed
) {
}

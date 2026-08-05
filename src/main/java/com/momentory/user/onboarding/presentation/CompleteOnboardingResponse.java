package com.momentory.user.onboarding.presentation;

import com.momentory.user.onboarding.application.CompleteOnboardingResult;
import io.swagger.v3.oas.annotations.media.Schema;

public record CompleteOnboardingResponse(
        @Schema(description = "사용자 ID", example = "1") Long userId,
        @Schema(description = "온보딩 필요 여부", example = "false") boolean onboardingRequired
) {

    static CompleteOnboardingResponse from(CompleteOnboardingResult result) {
        return new CompleteOnboardingResponse(result.userId(), result.onboardingRequired());
    }
}

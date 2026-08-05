package com.momentory.user.me.presentation;

import com.momentory.user.me.application.UserMeResult;
import io.swagger.v3.oas.annotations.media.Schema;

public record UserMeResponse(
        @Schema(description = "사용자 ID", example = "1") Long userId,
        @Schema(description = "현재 사용자 역할", example = "USER") String role,
        @Schema(description = "온보딩 필요 여부", example = "true") boolean onboardingRequired
) {
    static UserMeResponse from(UserMeResult result) {
        return new UserMeResponse(
                result.userId(),
                result.role().name(),
                result.onboardingRequired()
        );
    }
}

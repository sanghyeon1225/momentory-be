package com.momentory.auth.logout.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(@Schema(description = "현재 기기의 모멘토리 Refresh Token") @NotBlank(message = "refreshToken은 필수입니다.") String refreshToken) {
}

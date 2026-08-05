package com.momentory.user.presentation;

import com.momentory.user.domain.Gender;
import com.momentory.user.domain.InterestArea;
import com.momentory.user.domain.UserProfile;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

public record CompleteOnboardingRequest(
        @Schema(description = "닉네임", example = "모리")
        @NotBlank(message = "nickname은 필수입니다.")
        @Size(max = UserProfile.NICKNAME_MAX_LENGTH, message = "nickname은 최대 10자입니다.")
        String nickname,
        @Schema(description = "나이", example = "25", nullable = true)
        @Min(value = 1, message = "age는 1 이상이어야 합니다.")
        @Max(value = 120, message = "age는 120 이하여야 합니다.")
        Integer age,
        @Schema(description = "성별", example = "FEMALE")
        @NotNull(message = "gender는 필수입니다.")
        Gender gender,
        @Schema(description = "관심 분야", example = "[\"CAREER\", \"RELATIONSHIP\"]")
        @NotEmpty(message = "interestAreas는 최소 1개 이상이어야 합니다.")
        Set<InterestArea> interestAreas,
        @Schema(description = "회고 시간", example = "21:30")
        @NotBlank(message = "reflectionTime은 필수입니다.")
        @Pattern(regexp = "^(?:[01]\\d|2[0-3]):[0-5]\\d$", message = "reflectionTime은 HH:mm 형식이어야 합니다.")
        String reflectionTime,
        @Schema(description = "프론트가 보고한 캘린더 연동 성공 여부", example = "true")
        @NotNull(message = "calendarIntegrationEnabled는 필수입니다.")
        Boolean calendarIntegrationEnabled
) {

    public CompleteOnboardingRequest {
        nickname = nickname == null ? null : nickname.strip();
    }

    public LocalTime toReflectionTime() {
        return LocalTime.parse(reflectionTime, DateTimeFormatter.ofPattern("HH:mm"));
    }
}

package com.momentory.user.presentation;

import com.momentory.user.application.OnboardingOptionsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Onboarding", description = "온보딩 API")
@RestController
@RequestMapping("/api/v1/onboarding")
public class OnboardingOptionsController {

    private final OnboardingOptionsService onboardingOptionsService;

    public OnboardingOptionsController(OnboardingOptionsService onboardingOptionsService) {
        this.onboardingOptionsService = onboardingOptionsService;
    }

    @Operation(summary = "온보딩 선택지 조회")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "선택지 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping(value = "/options", produces = MediaType.APPLICATION_JSON_VALUE)
    public OnboardingOptionsResponse getOptions() {
        return OnboardingOptionsResponse.from(onboardingOptionsService.getOptions());
    }
}

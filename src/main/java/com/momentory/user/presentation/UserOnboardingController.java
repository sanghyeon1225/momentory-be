package com.momentory.user.presentation;

import com.momentory.auth.presentation.Login;
import com.momentory.auth.security.LoginPrincipal;
import com.momentory.user.application.CompleteOnboardingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Users", description = "사용자 API")
@RestController
@RequestMapping("/api/v1/users/me")
public class UserOnboardingController {

    private final CompleteOnboardingService completeOnboardingService;

    public UserOnboardingController(CompleteOnboardingService completeOnboardingService) {
        this.completeOnboardingService = completeOnboardingService;
    }

    @Operation(summary = "온보딩 완료 또는 갱신")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "온보딩 저장 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PutMapping(value = "/onboarding", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public CompleteOnboardingResponse complete(
            @Login LoginPrincipal principal,
            @Valid @RequestBody CompleteOnboardingRequest request
    ) {
        return CompleteOnboardingResponse.from(completeOnboardingService.complete(
                principal.userId(),
                request.nickname(),
                request.age(),
                request.gender(),
                request.interestAreas(),
                request.toReflectionTime(),
                request.calendarIntegrationEnabled()
        ));
    }
}

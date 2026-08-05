package com.momentory.auth.logout.presentation;

import com.momentory.auth.logout.application.LogoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class LogoutController {
    private final LogoutService logoutService;
    public LogoutController(LogoutService logoutService) { this.logoutService = logoutService; }
    @Operation(summary = "현재 기기 로그아웃", description = "제출된 Refresh Token 하나만 멱등적으로 폐기합니다.")
    @ApiResponse(responseCode = "204", description = "로그아웃 완료")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/logout", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void logout(@Valid @RequestBody LogoutRequest request) { logoutService.logout(request.refreshToken()); }
}

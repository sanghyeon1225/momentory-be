package com.momentory.auth.logout.presentation;

import com.momentory.auth.logout.application.LogoutService;
import com.momentory.auth.presentation.AuthErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "인증 API")
@RestController
@RequestMapping("/api/v1/auth")
public class LogoutController {
    private final LogoutService logoutService;
    public LogoutController(LogoutService logoutService) { this.logoutService = logoutService; }
    @Operation(summary = "현재 기기 로그아웃", description = "제출된 Refresh Token 하나만 멱등적으로 폐기합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "로그아웃 완료"),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "VALIDATION_ERROR",
                                            value = """
                                                    {
                                                      "code": "INVALID_REQUEST",
                                                      "message": "refreshToken은 필수입니다."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "UNREADABLE_REQUEST",
                                            value = """
                                                    {
                                                      "code": "INVALID_REQUEST",
                                                      "message": "잘못된 요청입니다."
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/logout", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void logout(@Valid @RequestBody LogoutRequest request) { logoutService.logout(request.refreshToken()); }
}

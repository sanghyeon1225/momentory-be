package com.momentory.auth.token.presentation;

import com.momentory.auth.token.application.RefreshTokenReissueService;
import com.momentory.auth.presentation.AuthErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class RefreshTokenReissueController {
    private final RefreshTokenReissueService reissueService;
    public RefreshTokenReissueController(RefreshTokenReissueService reissueService) { this.reissueService = reissueService; }

    @Operation(summary = "Access Token 재발급", description = "Refresh Token을 회전시켜 새 Access Token과 Refresh Token을 발급합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "재발급 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RefreshTokenReissueResponse.class)
                    )
            ),
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
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "유효하지 않거나 폐기·만료된 Refresh Token",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "REFRESH_TOKEN_INVALID",
                                            value = """
                                                    {
                                                      "code": "REFRESH_TOKEN_INVALID",
                                                      "message": "Refresh Token이 유효하지 않습니다."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "REFRESH_TOKEN_REVOKED",
                                            value = """
                                                    {
                                                      "code": "REFRESH_TOKEN_REVOKED",
                                                      "message": "Refresh Token이 이미 폐기되었습니다."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "REFRESH_TOKEN_EXPIRED",
                                            value = """
                                                    {
                                                      "code": "REFRESH_TOKEN_EXPIRED",
                                                      "message": "Refresh Token이 만료되었습니다."
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    @PostMapping(value = "/reissue", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public RefreshTokenReissueResponse reissue(@Valid @RequestBody RefreshTokenReissueRequest request) {
        return RefreshTokenReissueResponse.from(reissueService.reissue(request.refreshToken()));
    }
}

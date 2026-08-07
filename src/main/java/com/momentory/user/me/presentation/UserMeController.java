package com.momentory.user.me.presentation;

import com.momentory.auth.security.Login;
import com.momentory.auth.security.LoginPrincipal;
import com.momentory.common.presentation.ApiErrorResponse;
import com.momentory.user.me.application.UserMeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Users", description = "사용자 API")
@RestController
@RequestMapping("/api/v1/users")
public class UserMeController {

    private final UserMeService userMeService;

    public UserMeController(UserMeService userMeService) {
        this.userMeService = userMeService;
    }

    @Operation(summary = "현재 로그인 사용자 조회", description = "현재 Access Token의 사용자 정보를 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자 조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserMeResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증이 없거나 유효하지 않음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "AUTHENTICATION_REQUIRED",
                                    value = """
                                            {
                                              "code": "AUTHENTICATION_REQUIRED",
                                              "message": "인증이 필요합니다."
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserMeResponse getUserMe(@Login LoginPrincipal principal) {
        return UserMeResponse.from(userMeService.getUserMe(principal.userId()));
    }
}

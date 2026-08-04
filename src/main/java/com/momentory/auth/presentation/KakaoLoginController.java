package com.momentory.auth.presentation;

import com.momentory.auth.application.KakaoLoginResult;
import com.momentory.auth.application.KakaoLoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "인증 API")
@RestController
@RequestMapping("/api/v1/auth")
public class KakaoLoginController {

    private final KakaoLoginService kakaoLoginService;

    public KakaoLoginController(KakaoLoginService kakaoLoginService) {
        this.kakaoLoginService = kakaoLoginService;
    }

    @Operation(
            summary = "카카오 Native 로그인",
            description = "React Native 카카오 Native SDK Access Token을 검증하고 모멘토리 토큰을 발급합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = KakaoLoginResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "accessToken": "발급된 모멘토리 Access Token",
                                      "refreshToken": "발급된 모멘토리 Refresh Token",
                                      "tokenType": "Bearer",
                                      "accessTokenExpiresIn": 1800,
                                      "userId": 1,
                                      "onboardingRequired": true
                                    }
                                    """)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "요청 Access Token 누락 또는 공백"),
            @ApiResponse(responseCode = "401", description = "카카오 토큰 검증 실패"),
            @ApiResponse(responseCode = "502", description = "카카오 API 서버 또는 응답 오류"),
            @ApiResponse(responseCode = "503", description = "카카오 API 네트워크 오류")
    })
    @PostMapping(value = "/kakao", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public KakaoLoginResponse login(@Valid @RequestBody KakaoLoginRequest request) {
        KakaoLoginResult result = kakaoLoginService.login(request.accessToken());
        return KakaoLoginResponse.from(result);
    }
}

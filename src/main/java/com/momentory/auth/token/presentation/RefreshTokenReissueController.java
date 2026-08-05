package com.momentory.auth.token.presentation;

import com.momentory.auth.token.application.RefreshTokenReissueService;
import io.swagger.v3.oas.annotations.Operation;
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
    @ApiResponses({@ApiResponse(responseCode = "200", description = "재발급 성공"), @ApiResponse(responseCode = "401", description = "유효하지 않거나 폐기·만료된 Refresh Token")})
    @PostMapping(value = "/reissue", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public RefreshTokenReissueResponse reissue(@Valid @RequestBody RefreshTokenReissueRequest request) {
        return RefreshTokenReissueResponse.from(reissueService.reissue(request.refreshToken()));
    }
}

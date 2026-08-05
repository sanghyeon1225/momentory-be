package com.momentory.auth.token.presentation;

import com.momentory.auth.token.application.RefreshTokenReissueResult;

public record RefreshTokenReissueResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long accessTokenExpiresIn
) {
    public static RefreshTokenReissueResponse from(RefreshTokenReissueResult result) {
        return new RefreshTokenReissueResponse(result.accessToken(), result.refreshToken(), "Bearer", result.accessTokenExpiresIn().toSeconds());
    }
}

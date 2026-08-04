package com.momentory.auth.presentation;

import com.momentory.auth.application.RefreshTokenReissueResult;

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

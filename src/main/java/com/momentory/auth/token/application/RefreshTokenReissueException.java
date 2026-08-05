package com.momentory.auth.token.application;

public final class RefreshTokenReissueException extends RuntimeException {

    private final RefreshTokenReissueErrorCode errorCode;

    public RefreshTokenReissueException(RefreshTokenReissueErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public RefreshTokenReissueErrorCode getErrorCode() {
        return errorCode;
    }
}

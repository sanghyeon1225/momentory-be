package com.momentory.auth.kakao.infrastructure;

public final class KakaoApiException extends RuntimeException {

    private final KakaoApiErrorCode errorCode;

    public KakaoApiException(KakaoApiErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public KakaoApiException(KakaoApiErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public KakaoApiErrorCode getErrorCode() {
        return errorCode;
    }
}

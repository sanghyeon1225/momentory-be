package com.momentory.auth.presentation;

import com.momentory.auth.infrastructure.kakao.KakaoApiErrorCode;
import com.momentory.auth.infrastructure.kakao.KakaoApiException;
import com.momentory.auth.application.RefreshTokenReissueException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = {KakaoLoginController.class, RefreshTokenReissueController.class, LogoutController.class})
public class KakaoLoginExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<AuthErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        FieldError fieldError = exception.getBindingResult().getFieldError();
        String message = fieldError == null ? "잘못된 요청입니다." : fieldError.getDefaultMessage();
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<AuthErrorResponse> handleUnreadableRequest() {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "잘못된 요청입니다.");
    }

    @ExceptionHandler(KakaoApiException.class)
    ResponseEntity<AuthErrorResponse> handleKakaoApiException(KakaoApiException exception) {
        return switch (exception.getErrorCode()) {
            case INVALID_ACCESS_TOKEN -> error(HttpStatus.UNAUTHORIZED, "KAKAO_TOKEN_INVALID", "카카오 인증에 실패했습니다.");
            case APP_ID_MISMATCH -> error(HttpStatus.UNAUTHORIZED, "KAKAO_APP_ID_MISMATCH", "카카오 인증에 실패했습니다.");
            case USER_ID_MISMATCH -> error(HttpStatus.UNAUTHORIZED, "KAKAO_USER_ID_MISMATCH", "카카오 인증에 실패했습니다.");
            case KAKAO_API_SERVER_ERROR -> error(HttpStatus.BAD_GATEWAY, "KAKAO_API_SERVER_ERROR", "카카오 서비스에 일시적인 오류가 발생했습니다.");
            case KAKAO_API_NETWORK_ERROR -> error(HttpStatus.SERVICE_UNAVAILABLE, "KAKAO_API_NETWORK_ERROR", "카카오 서비스에 연결할 수 없습니다.");
            case UNEXPECTED_KAKAO_RESPONSE -> error(HttpStatus.BAD_GATEWAY, "KAKAO_API_RESPONSE_ERROR", "카카오 서비스 응답을 처리할 수 없습니다.");
        };
    }

    @ExceptionHandler(RefreshTokenReissueException.class)
    ResponseEntity<AuthErrorResponse> handleRefreshToken(RefreshTokenReissueException exception) {
        return switch (exception.getErrorCode()) {
            case INVALID -> error(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_INVALID", "Refresh Token이 유효하지 않습니다.");
            case REVOKED -> error(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_REVOKED", "Refresh Token이 이미 폐기되었습니다.");
            case EXPIRED -> error(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_EXPIRED", "Refresh Token이 만료되었습니다.");
        };
    }

    private ResponseEntity<AuthErrorResponse> error(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(new AuthErrorResponse(code, message));
    }
}

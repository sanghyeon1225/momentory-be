package com.momentory.user.onboarding.presentation;

import com.momentory.user.application.AuthenticatedUserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = {UserOnboardingController.class, OnboardingOptionsController.class})
public class OnboardingExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<OnboardingErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        FieldError fieldError = exception.getBindingResult().getFieldError();
        String message = fieldError == null ? "잘못된 요청입니다." : fieldError.getDefaultMessage();
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<OnboardingErrorResponse> handleUnreadableRequest() {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "잘못된 요청입니다.");
    }

    @ExceptionHandler(AuthenticatedUserNotFoundException.class)
    ResponseEntity<OnboardingErrorResponse> handleAuthenticatedUserNotFound() {
        return error(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_REQUIRED", "인증이 필요합니다.");
    }

    private ResponseEntity<OnboardingErrorResponse> error(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(new OnboardingErrorResponse(code, message));
    }
}

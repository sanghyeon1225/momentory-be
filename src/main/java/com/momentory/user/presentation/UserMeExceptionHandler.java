package com.momentory.user.presentation;

import com.momentory.auth.presentation.AuthErrorResponse;
import com.momentory.user.application.AuthenticatedUserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = UserMeController.class)
public class UserMeExceptionHandler {

    @ExceptionHandler(AuthenticatedUserNotFoundException.class)
    ResponseEntity<AuthErrorResponse> handleAuthenticatedUserNotFound() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new AuthErrorResponse("AUTHENTICATION_REQUIRED", "인증이 필요합니다."));
    }
}

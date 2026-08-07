package com.momentory.memo.presentation;

import com.momentory.auth.presentation.AuthErrorResponse;
import com.momentory.memo.application.DailyMemoNotFoundException;
import com.momentory.user.application.AuthenticatedUserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice(assignableTypes = DailyMemoController.class)
public class DailyMemoExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<DailyMemoErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        FieldError fieldError = exception.getBindingResult().getFieldError();
        String message = fieldError == null ? "Invalid request." : fieldError.getDefaultMessage();
        return dailyMemoError(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", message);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    ResponseEntity<DailyMemoErrorResponse> handleInvalidRequest() {
        return dailyMemoError(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Invalid request.");
    }

    @ExceptionHandler(DailyMemoNotFoundException.class)
    ResponseEntity<DailyMemoErrorResponse> handleDailyMemoNotFound() {
        return dailyMemoError(HttpStatus.NOT_FOUND, "DAILY_MEMO_NOT_FOUND", "Daily memo not found.");
    }

    @ExceptionHandler(AuthenticatedUserNotFoundException.class)
    ResponseEntity<AuthErrorResponse> handleAuthenticatedUserNotFound() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new AuthErrorResponse("AUTHENTICATION_REQUIRED", "인증이 필요합니다."));
    }

    private ResponseEntity<DailyMemoErrorResponse> dailyMemoError(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(new DailyMemoErrorResponse(code, message));
    }
}

package com.momentory.memo.presentation;

import com.momentory.common.presentation.ApiErrorResponse;
import com.momentory.memo.application.DailyMemoNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = DailyMemoController.class)
public class DailyMemoExceptionHandler {

    @ExceptionHandler(DailyMemoNotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleDailyMemoNotFound() {
        return dailyMemoError(HttpStatus.NOT_FOUND, "DAILY_MEMO_NOT_FOUND", "하루 메모를 찾을 수 없습니다.");
    }

    private ResponseEntity<ApiErrorResponse> dailyMemoError(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(code, message));
    }
}

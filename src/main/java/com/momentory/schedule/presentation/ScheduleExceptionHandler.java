package com.momentory.schedule.presentation;

import com.momentory.auth.presentation.AuthErrorResponse;
import com.momentory.schedule.application.ScheduleNotFoundException;
import com.momentory.schedule.application.InvalidScheduleOrderException;
import com.momentory.user.application.AuthenticatedUserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice(assignableTypes = ScheduleController.class)
public class ScheduleExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ScheduleErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        FieldError fieldError = exception.getBindingResult().getFieldError();
        String message = fieldError == null ? "잘못된 요청입니다." : fieldError.getDefaultMessage();
        return scheduleError(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ScheduleErrorResponse> handleUnreadableRequest() {
        return scheduleError(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "잘못된 요청입니다.");
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
    ResponseEntity<ScheduleErrorResponse> handleInvalidParameter() {
        return scheduleError(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "잘못된 요청입니다.");
    }

    @ExceptionHandler(ScheduleNotFoundException.class)
    ResponseEntity<ScheduleErrorResponse> handleScheduleNotFound() {
        return scheduleError(HttpStatus.NOT_FOUND, "SCHEDULE_NOT_FOUND", "일정을 찾을 수 없습니다.");
    }

    @ExceptionHandler(InvalidScheduleOrderException.class)
    ResponseEntity<ScheduleErrorResponse> handleInvalidScheduleOrder() {
        return scheduleError(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Invalid schedule order request.");
    }

    @ExceptionHandler(AuthenticatedUserNotFoundException.class)
    ResponseEntity<AuthErrorResponse> handleAuthenticatedUserNotFound() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new AuthErrorResponse("AUTHENTICATION_REQUIRED", "인증이 필요합니다."));
    }

    private ResponseEntity<ScheduleErrorResponse> scheduleError(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(new ScheduleErrorResponse(code, message));
    }
}

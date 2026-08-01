package com.momentory.auth.presentation;

public record AuthErrorResponse(
        String code,
        String message
) {
}

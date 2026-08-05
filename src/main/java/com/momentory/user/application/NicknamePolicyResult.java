package com.momentory.user.application;

public record NicknamePolicyResult(
        int maxLength,
        boolean duplicateAllowed
) {
}

package com.momentory.auth.security;

import com.momentory.auth.domain.MemberRole;

public record LoginPrincipal(
        Long memberId,
        MemberRole role
) {
}

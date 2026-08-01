package com.momentory.auth;

import com.momentory.auth.presentation.Login;
import com.momentory.auth.security.LoginPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class AuthenticationTestController {

    @GetMapping("/test-auth/me")
    TestResponse me(@Login LoginPrincipal principal) {
        return new TestResponse(principal.memberId(), principal.role().name());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/test-auth/admin")
    TestResponse admin(@Login LoginPrincipal principal) {
        return new TestResponse(principal.memberId(), principal.role().name());
    }

    record TestResponse(Long memberId, String role) {
    }
}

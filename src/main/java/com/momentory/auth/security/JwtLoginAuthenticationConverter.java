package com.momentory.auth.security;

import com.momentory.user.domain.UserRole;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;

import java.util.List;

public final class JwtLoginAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Long userId = parseUserId(jwt.getSubject());
        UserRole role = parseRole(jwt.getClaimAsString("role"));

        LoginPrincipal principal = new LoginPrincipal(userId, role);
        return new LoginAuthenticationToken(principal, List.of(new SimpleGrantedAuthority("ROLE_" + role.name())));
    }

    private Long parseUserId(String subject) {
        try {
            return Long.valueOf(subject);
        } catch (NumberFormatException exception) {
            throw new InvalidBearerTokenException("JWT subject must be a user ID", exception);
        }
    }

    private UserRole parseRole(String role) {
        try {
            return UserRole.valueOf(role);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new InvalidBearerTokenException("JWT role is invalid", exception);
        }
    }
}

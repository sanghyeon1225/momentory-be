package com.momentory.auth.security;

import com.momentory.auth.domain.MemberRole;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;

import java.util.List;

public final class JwtLoginAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Long memberId = parseMemberId(jwt.getSubject());
        MemberRole role = parseRole(jwt.getClaimAsString("role"));

        LoginPrincipal principal = new LoginPrincipal(memberId, role);
        return new LoginAuthenticationToken(principal, List.of(new SimpleGrantedAuthority("ROLE_" + role.name())));
    }

    private Long parseMemberId(String subject) {
        try {
            return Long.valueOf(subject);
        } catch (NumberFormatException exception) {
            throw new InvalidBearerTokenException("JWT subject must be a member ID", exception);
        }
    }

    private MemberRole parseRole(String role) {
        try {
            return MemberRole.valueOf(role);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new InvalidBearerTokenException("JWT role is invalid", exception);
        }
    }
}

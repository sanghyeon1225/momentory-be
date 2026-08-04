package com.momentory.auth.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/** Minimal authenticated token that exposes {@link LoginPrincipal} as its principal. */
public final class LoginAuthenticationToken extends AbstractAuthenticationToken {

    private final LoginPrincipal principal;

    public LoginAuthenticationToken(LoginPrincipal principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public LoginPrincipal getPrincipal() {
        return principal;
    }

    @Override
    public String getName() {
        return principal.userId().toString();
    }
}

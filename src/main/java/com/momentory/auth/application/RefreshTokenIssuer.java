package com.momentory.auth.application;

import com.momentory.auth.security.JwtProperties;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Component
public class RefreshTokenIssuer {

    private static final int TOKEN_BYTE_LENGTH = 32;

    private final JwtProperties jwtProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenIssuer(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public IssuedRefreshToken issue() {
        byte[] bytes = new byte[TOKEN_BYTE_LENGTH];
        secureRandom.nextBytes(bytes);

        String value = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return new IssuedRefreshToken(
                value,
                hash(value),
                Instant.now().plus(jwtProperties.refreshExpiration())
        );
    }

    public String hash(String value) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 must be available.", exception);
        }
    }
}

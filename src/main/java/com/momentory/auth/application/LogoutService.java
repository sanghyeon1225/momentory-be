package com.momentory.auth.application;

import com.momentory.auth.infrastructure.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LogoutService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenIssuer refreshTokenIssuer;

    public LogoutService(RefreshTokenRepository refreshTokenRepository, RefreshTokenIssuer refreshTokenIssuer) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenIssuer = refreshTokenIssuer;
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByTokenHashForUpdate(refreshTokenIssuer.hash(refreshToken))
                .ifPresent(token -> {
                    if (!token.isRevoked()) token.revoke();
                });
    }
}

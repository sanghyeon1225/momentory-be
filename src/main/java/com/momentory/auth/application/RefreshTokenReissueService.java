package com.momentory.auth.application;

import com.momentory.auth.domain.RefreshToken;
import com.momentory.auth.infrastructure.RefreshTokenRepository;
import com.momentory.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class RefreshTokenReissueService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenIssuer refreshTokenIssuer;
    private final AccessTokenIssuer accessTokenIssuer;

    public RefreshTokenReissueService(
            RefreshTokenRepository refreshTokenRepository,
            RefreshTokenIssuer refreshTokenIssuer,
            AccessTokenIssuer accessTokenIssuer
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenIssuer = refreshTokenIssuer;
        this.accessTokenIssuer = accessTokenIssuer;
    }

    @Transactional
    public RefreshTokenReissueResult reissue(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHashForUpdate(refreshTokenIssuer.hash(refreshTokenValue))
                .orElseThrow(() -> new RefreshTokenReissueException(RefreshTokenReissueErrorCode.INVALID));
        if (refreshToken.isRevoked()) {
            throw new RefreshTokenReissueException(RefreshTokenReissueErrorCode.REVOKED);
        }
        if (refreshToken.isExpired(Instant.now())) {
            throw new RefreshTokenReissueException(RefreshTokenReissueErrorCode.EXPIRED);
        }

        User user = refreshToken.getUser();
        refreshToken.revoke();
        IssuedAccessToken accessToken = accessTokenIssuer.issue(user.getId(), user.getRole());
        IssuedRefreshToken newRefreshToken = refreshTokenIssuer.issue();
        refreshTokenRepository.save(RefreshToken.create(user, newRefreshToken.hash(), newRefreshToken.expiresAt()));

        return new RefreshTokenReissueResult(
                accessToken.value(),
                newRefreshToken.value(),
                accessToken.expiresIn()
        );
    }
}

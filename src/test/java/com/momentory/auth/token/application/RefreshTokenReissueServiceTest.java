package com.momentory.auth.token.application;

import com.momentory.auth.token.domain.RefreshToken;
import com.momentory.auth.token.infrastructure.RefreshTokenRepository;
import com.momentory.user.domain.User;
import com.momentory.user.domain.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenReissueServiceTest {
    @Mock RefreshTokenRepository repository;
    @Mock RefreshTokenIssuer refreshTokenIssuer;
    @Mock AccessTokenIssuer accessTokenIssuer;
    @Mock RefreshToken refreshToken;
    @Mock User user;
    @InjectMocks RefreshTokenReissueService service;

    @Test
    void rotatesValidRefreshToken() {
        when(refreshTokenIssuer.hash("old")).thenReturn("old-hash");
        when(repository.findByTokenHashForUpdate("old-hash")).thenReturn(Optional.of(refreshToken));
        when(refreshToken.isRevoked()).thenReturn(false);
        when(refreshToken.isExpired(any())).thenReturn(false);
        when(refreshToken.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(1L);
        when(user.getRole()).thenReturn(UserRole.USER);
        when(accessTokenIssuer.issue(1L, UserRole.USER)).thenReturn(new IssuedAccessToken("access", Duration.ofMinutes(30)));
        when(refreshTokenIssuer.issue()).thenReturn(new IssuedRefreshToken("new", "new-hash", Instant.now().plusSeconds(60)));

        RefreshTokenReissueResult result = service.reissue("old");

        assertThat(result.accessToken()).isEqualTo("access");
        assertThat(result.refreshToken()).isEqualTo("new");
        verify(refreshToken).revoke();
        verify(repository).save(any(RefreshToken.class));
    }

    @Test
    void rejectsMissingOrRevokedOrExpiredTokenWithoutSavingNewToken() {
        when(refreshTokenIssuer.hash("missing")).thenReturn("hash");
        when(repository.findByTokenHashForUpdate("hash")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.reissue("missing")).isInstanceOf(RefreshTokenReissueException.class);
        verify(repository, never()).save(any());
    }
}

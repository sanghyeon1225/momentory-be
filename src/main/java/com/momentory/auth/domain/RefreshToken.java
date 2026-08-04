package com.momentory.auth.domain;

import com.momentory.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
        name = "refresh_tokens",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_refresh_tokens_token_hash",
                columnNames = "token_hash"
        )
)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_refresh_tokens_user")
    )
    private User user;

    @Column(name = "token_hash", nullable = false, length = 255)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected RefreshToken() {
    }

    private RefreshToken(User user, String tokenHash, Instant expiresAt) {
        this.user = Objects.requireNonNull(user);
        this.tokenHash = requireTokenHash(tokenHash);
        this.expiresAt = Objects.requireNonNull(expiresAt);
    }

    public static RefreshToken create(User user, String tokenHash, Instant expiresAt) {
        return new RefreshToken(user, tokenHash, expiresAt);
    }

    public void revoke() {
        if (revokedAt == null) {
            revokedAt = Instant.now();
        }
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isExpired(Instant now) {
        return !expiresAt.isAfter(now);
    }

    @PrePersist
    protected void initializeCreatedAt() {
        createdAt = Instant.now();
    }

    private String requireTokenHash(String tokenHash) {
        if (tokenHash == null || tokenHash.isBlank()) {
            throw new IllegalArgumentException("Token hash must not be blank.");
        }
        return tokenHash;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

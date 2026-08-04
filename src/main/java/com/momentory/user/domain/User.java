package com.momentory.user.domain;

import com.momentory.common.persistence.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(name = "onboarding_completed", nullable = false)
    private boolean onboardingCompleted;

    protected User() {
    }

    private User(UserRole role) {
        this.role = role;
        this.onboardingCompleted = false;
    }

    public static User create() {
        return new User(UserRole.USER);
    }

    public void completeOnboarding() {
        onboardingCompleted = true;
    }

    public boolean requiresOnboarding() {
        return !onboardingCompleted;
    }

    public Long getId() {
        return id;
    }

    public UserRole getRole() {
        return role;
    }
}

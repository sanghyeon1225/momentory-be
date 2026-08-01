package com.momentory.auth.domain;

/**
 * Temporary authentication role until the member domain is introduced.
 * Move this type to member.domain.MemberRole at that time without creating a duplicate enum.
 */
public enum MemberRole {
    USER,
    ADMIN
}

package com.momentory.user.domain;

public enum Gender {
    MALE("남성"),
    FEMALE("여성"),
    UNSPECIFIED("선택하지 않음");

    private final String label;

    Gender(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

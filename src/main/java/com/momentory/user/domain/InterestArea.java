package com.momentory.user.domain;

public enum InterestArea {
    STUDY("학업"),
    CAREER("취업·진로"),
    WORK("일·직장생활"),
    RELATIONSHIP("인간관계"),
    FAMILY("가족"),
    SELF("내 자신"),
    HEALTH("건강"),
    OTHER("기타");

    private final String label;

    InterestArea(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

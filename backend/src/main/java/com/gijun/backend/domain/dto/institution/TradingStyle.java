package com.gijun.backend.domain.dto.institution;

public enum TradingStyle {
    AGGRESSIVE("공격적 매매"),
    CONSERVATIVE("보수적 매매"),
    VALUE_FOCUSED("가치투자"),
    MOMENTUM_BASED("모멘텀 추종");

    private final String description;

    TradingStyle(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
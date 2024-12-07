package com.gijun.backend.domain.dto.account;

public enum AccountStatus {
    ACTIVE("활성"),
    SUSPENDED("정지"),
    CLOSED("해지");

    private final String description;

    AccountStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
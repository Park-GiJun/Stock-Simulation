package com.gijun.backend.domain.dto.order;

public enum OrderStatus {
    PENDING("대기"),
    COMPLETED("체결"),
    CANCELLED("취소"),
    FAILED("실패");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
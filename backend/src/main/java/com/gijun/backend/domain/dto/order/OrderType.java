package com.gijun.backend.domain.dto.order;

public enum OrderType {
    BUY("매수"),
    SELL("매도");

    private final String description;

    OrderType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

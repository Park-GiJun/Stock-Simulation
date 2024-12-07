package com.gijun.backend.domain.dto.order;

public enum TransactionType {
    DEPOSIT("입금"),
    WITHDRAWAL("출금"),
    BUY("매수"),
    SELL("매도");

    private final String description;

    TransactionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
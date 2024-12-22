package com.gijun.backend.domain.dto.order;

import com.gijun.backend.domain.entity.UserStock;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class UserStockResponseDto {
    private final String stockCode;
    private final String stockName;
    private final Integer quantity;
    private final BigDecimal averagePrice;
    private final BigDecimal currentValue;
    private final BigDecimal totalGain;
    private final Double gainRate;

    public UserStockResponseDto(UserStock userStock) {
        this.stockCode = userStock.getStock().getStockCode();
        this.stockName = userStock.getStock().getCompanyName();
        this.quantity = userStock.getQuantity();
        this.averagePrice = userStock.getAveragePrice();
        this.currentValue = userStock.getCurrentValue();
        this.totalGain = userStock.getTotalGain();
        this.gainRate = userStock.getGainRate();
    }
}
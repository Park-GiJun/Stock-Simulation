package com.gijun.backend.domain.dto.order;

import com.gijun.backend.domain.entity.Order;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class OrderResponseDto {
    private final Long orderId;
    private final String stockCode;
    private final String stockName;
    private final OrderType orderType;
    private final OrderStatus status;
    private final Integer quantity;
    private final BigDecimal orderPrice;
    private final BigDecimal totalAmount;
    private final LocalDateTime createdAt;
    private final LocalDateTime executedAt;
    private final String cancelReason;

    public OrderResponseDto(Order order) {
        this.orderId = order.getId();
        this.stockCode = order.getStock().getStockCode();
        this.stockName = order.getStock().getCompanyName();
        this.orderType = order.getOrderType();
        this.status = order.getStatus();
        this.quantity = order.getQuantity();
        this.orderPrice = order.getOrderPrice();
        this.totalAmount = order.getTotalAmount();
        this.createdAt = order.getCreatedAt();
        this.executedAt = order.getExecutedAt();
        this.cancelReason = order.getCancelReason();
    }
}
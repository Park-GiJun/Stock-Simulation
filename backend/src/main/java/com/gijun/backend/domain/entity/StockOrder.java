package com.gijun.backend.domain.entity;

import com.gijun.backend.domain.dto.order.OrderStatus;
import com.gijun.backend.domain.dto.order.OrderType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockOrder extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private LocalDateTime orderedAt;

    private LocalDateTime executedAt;

    @Builder
    public StockOrder(Account account, Stock stock, OrderType type,
                      Integer quantity, BigDecimal price) {
        this.account = account;
        this.stock = stock;
        this.type = type;
        this.status = OrderStatus.PENDING;
        this.quantity = quantity;
        this.price = price;
        this.orderedAt = LocalDateTime.now();
    }

    public void execute() {
        this.status = OrderStatus.EXECUTED;
        this.executedAt = LocalDateTime.now();
    }
}
package com.gijun.backend.domain.entity;

import com.gijun.backend.domain.dto.order.OrderStatus;
import com.gijun.backend.domain.dto.order.OrderType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 추가된 필드

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Enumerated(EnumType.STRING)
    private OrderType orderType;  // BUY, SELL

    @Enumerated(EnumType.STRING)
    private OrderStatus status;   // PENDING, COMPLETED, CANCELLED, FAILED

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal orderPrice;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    private LocalDateTime executedAt;
    private String cancelReason;

    @Builder
    public Order(User user, Account account, Stock stock, OrderType orderType, Integer quantity, BigDecimal orderPrice) {
        this.user = user;
        this.account = account;
        this.stock = stock;
        this.orderType = orderType;
        this.quantity = quantity;
        this.orderPrice = orderPrice;
        this.totalAmount = orderPrice.multiply(BigDecimal.valueOf(quantity));
        this.status = OrderStatus.PENDING;
    }

    public void complete() {
        this.status = OrderStatus.COMPLETED;
        this.executedAt = LocalDateTime.now();
    }

    public void cancel(String reason) {
        this.status = OrderStatus.CANCELLED;
        this.cancelReason = reason;
    }

    public void fail(String reason) {
        this.status = OrderStatus.FAILED;
        this.cancelReason = reason;
    }
}

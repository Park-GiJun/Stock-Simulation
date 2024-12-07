package com.gijun.backend.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_stocks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserStock extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal averagePrice;

    @Column(nullable = false)
    private BigDecimal currentValue;

    @Column(nullable = false)
    private BigDecimal totalGain;

    @Column(nullable = false)
    private Double gainRate;

    @Builder
    public UserStock(Account account, Stock stock, Integer quantity, BigDecimal averagePrice) {
        this.account = account;
        this.stock = stock;
        this.quantity = quantity;
        this.averagePrice = averagePrice;
        this.currentValue = averagePrice.multiply(BigDecimal.valueOf(quantity));
        this.totalGain = BigDecimal.ZERO;
        this.gainRate = 0.0;
    }

    public void updateValue(BigDecimal currentPrice) {
        this.currentValue = currentPrice.multiply(BigDecimal.valueOf(quantity));
        this.totalGain = this.currentValue.subtract(averagePrice.multiply(BigDecimal.valueOf(quantity)));
        this.gainRate = this.totalGain.divide(averagePrice.multiply(BigDecimal.valueOf(quantity)), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue();
    }
}
package com.gijun.backend.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_price_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockPriceHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private BigDecimal changeRate;

    @Column(nullable = false)
    private Integer volume;

    @Builder
    public StockPriceHistory(Stock stock, BigDecimal price, BigDecimal changeRate, Integer volume) {
        this.stock = stock;
        this.timestamp = LocalDateTime.now();
        this.price = price;
        this.changeRate = changeRate;
        this.volume = volume;
    }
}
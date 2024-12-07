package com.gijun.backend.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stocks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String stockCode;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private BigDecimal basePrice;

    @Column(nullable = false)
    private BigDecimal currentPrice;

    @Column(nullable = false)
    private BigDecimal dailyHigh;

    @Column(nullable = false)
    private BigDecimal dailyLow;

    @Column(nullable = false)
    private Integer dailyVolume;

    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    @Column(nullable = false)
    private Boolean isTrading;

    @Builder
    public Stock(String stockCode, String companyName, BigDecimal basePrice) {
        this.stockCode = stockCode;
        this.companyName = companyName;
        this.basePrice = basePrice;
        this.currentPrice = basePrice;
        this.dailyHigh = basePrice;
        this.dailyLow = basePrice;
        this.dailyVolume = 0;
        this.lastUpdated = LocalDateTime.now();
        this.isTrading = true;
    }

    public void updatePrice(BigDecimal newPrice, Integer volume) {
        this.currentPrice = newPrice;
        this.dailyHigh = newPrice.max(this.dailyHigh);
        this.dailyLow = newPrice.min(this.dailyLow);
        this.dailyVolume += volume;
        this.lastUpdated = LocalDateTime.now();
    }

    public void resetDaily() {
        this.basePrice = this.currentPrice;
        this.dailyHigh = this.currentPrice;
        this.dailyLow = this.currentPrice;
        this.dailyVolume = 0;
    }
}
package com.gijun.backend.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "institution_holdings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InstitutionHolding extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal averagePrice;

    @Builder
    public InstitutionHolding(Institution institution, Stock stock,
                              Integer quantity, BigDecimal averagePrice) {
        this.institution = institution;
        this.stock = stock;
        this.quantity = quantity;
        this.averagePrice = averagePrice;
    }
}
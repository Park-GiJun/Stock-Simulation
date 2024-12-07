package com.gijun.backend.domain.entity;

import com.gijun.backend.domain.dto.institution.TradingStyle;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "institution_characteristics")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InstitutionCharacteristics extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @Column(nullable = false)
    private Double riskTolerance;  // 0~1

    @Column(nullable = false)
    private Double tradingFrequency;  // 0~1

    @Column(nullable = false)
    private Double positionLimit;  // 최대 보유 한도 (%)

    @Column(nullable = false)
    private Double cashReserveRatio;  // 현금 보유 비율 (%)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TradingStyle tradingStyle;

    @Builder
    public InstitutionCharacteristics(Institution institution, Double riskTolerance,
                                      Double tradingFrequency, Double positionLimit,
                                      Double cashReserveRatio, TradingStyle tradingStyle) {
        this.institution = institution;
        this.riskTolerance = riskTolerance;
        this.tradingFrequency = tradingFrequency;
        this.positionLimit = positionLimit;
        this.cashReserveRatio = cashReserveRatio;
        this.tradingStyle = tradingStyle;
    }
}
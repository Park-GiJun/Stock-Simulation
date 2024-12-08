package com.gijun.backend.domain.entity;

import com.gijun.backend.domain.dto.institution.InstitutionType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "institutions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Institution extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InstitutionType type;

    @Column(nullable = false)
    private BigDecimal totalAsset;

    @Column(nullable = false)
    private BigDecimal cashBalance;

    @Builder
    public Institution(String code, String name, InstitutionType type,
                       BigDecimal totalAsset, BigDecimal cashBalance) {
        this.code = code;
        this.name = name;
        this.type = type;
        this.totalAsset = totalAsset;
        this.cashBalance = cashBalance;
    }

    public void updateCashBalance(BigDecimal newCash) {
        this.cashBalance = newCash;
    }

    public void updateTotalAsset(BigDecimal newTotalAsset) {
        this.totalAsset = newTotalAsset;
    }
}

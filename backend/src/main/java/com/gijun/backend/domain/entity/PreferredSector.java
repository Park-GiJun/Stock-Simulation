package com.gijun.backend.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "preferred_sectors")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PreferredSector extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @Column(nullable = false)
    private String sectorCode;

    @Column(nullable = false)
    private Double weight;  // 선호도 가중치 (0~1)

    @Builder
    public PreferredSector(Institution institution, String sectorCode, Double weight) {
        this.institution = institution;
        this.sectorCode = sectorCode;
        this.weight = weight;
    }
}

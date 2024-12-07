package com.gijun.backend.domain.dto.institution;

public enum InstitutionType {
    PENSION_FUND("연기금"),
    INSURANCE("보험사"),
    INVESTMENT_BANK("투자은행"),
    ASSET_MANAGEMENT("자산운용사");

    private final String description;

    InstitutionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
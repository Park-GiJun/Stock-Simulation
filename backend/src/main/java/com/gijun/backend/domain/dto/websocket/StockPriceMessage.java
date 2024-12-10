package com.gijun.backend.domain.dto.websocket;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.gijun.backend.utils.BigDecimalArrayDeserializer;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StockPriceMessage {
    private String stockCode;
    private String companyName;

    @JsonDeserialize(using = BigDecimalArrayDeserializer.class)
    private BigDecimal currentPrice;

    @JsonDeserialize(using = BigDecimalArrayDeserializer.class)
    private BigDecimal previousPrice;

    private Long volume;
    private String timestamp;
}

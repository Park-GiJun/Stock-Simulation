package com.gijun.backend.domain.dto.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderRequestDto {
    @NotBlank(message = "종목 코드는 필수입니다")
    private String stockCode;

    @NotNull(message = "주문 수량은 필수입니다")
    @Min(value = 10, message = "최소 주문 수량은 10주입니다")
    private Integer quantity;

    @NotNull(message = "주문 가격은 필수입니다")
    @Min(value = 1, message = "주문 가격은 1원 이상이어야 합니다")
    private BigDecimal price;
}
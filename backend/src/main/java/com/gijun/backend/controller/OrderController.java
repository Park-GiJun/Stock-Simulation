package com.gijun.backend.controller;

import com.gijun.backend.domain.dto.common.commonResponse;
import com.gijun.backend.domain.dto.order.*;
import com.gijun.backend.domain.entity.Account;
import com.gijun.backend.domain.entity.Order;
import com.gijun.backend.domain.entity.User;
import com.gijun.backend.domain.entity.UserStock;
import com.gijun.backend.service.AccountService;
import com.gijun.backend.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Stock Trading", description = "주식 거래 관련 API")
@RestController
@RequestMapping("/trading")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final AccountService accountService;

    @Operation(summary = "계좌 요약 정보 조회", description = "계좌의 잔고, 총 투자금액, 총 평가금액을 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = AccountSummaryResponseDto.class))
            )
    })
    @GetMapping("/account/summary")
    public commonResponse<AccountSummaryResponseDto> getAccountSummary(
            @AuthenticationPrincipal User user) {
        log.info("Get Account Summary - UserId: {}", user.getId());
        Account account = accountService.getAccountByUserId(user.getId());
        return commonResponse.success(new AccountSummaryResponseDto(account));
    }

    @Operation(summary = "주식 매수 주문", description = "지정한 가격으로 주식 매수 주문을 생성합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "주문 생성 성공",
                    content = @Content(schema = @Schema(implementation = OrderResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 주문 (가격, 수량 등) 또는 잔고 부족"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 종목"
            )
    })
    @PostMapping("/order/buy")
    public commonResponse<OrderResponseDto> createBuyOrder(
            @AuthenticationPrincipal(expression = "user") User user,
            @Valid @RequestBody CreateOrderRequestDto request) {

        if (user == null) {
            log.warn("User is not authenticated");
        }

        log.info("Buy Order Request - UserId: {}, Stock: {}, Quantity: {}, Price: {}",
                user.getId(), request.getStockCode(), request.getQuantity(), request.getPrice());

        Account account = accountService.getAccountByUserId(user.getId());
        Order order = orderService.createOrder(
                account,
                request.getStockCode(),
                OrderType.BUY,
                user,
                request.getQuantity(),
                request.getPrice()
        );

        return commonResponse.success(new OrderResponseDto(order));
    }

    @Operation(summary = "주식 매도 주문", description = "지정한 가격으로 주식 매도 주문을 생성합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "주문 생성 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 주문 (가격, 수량) 또는 보유 수량 부족"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 종목"
            )
    })
    @PostMapping("/order/sell")
    public commonResponse<OrderResponseDto> createSellOrder(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateOrderRequestDto request) {
        log.info("Sell Order Request - UserId: {}, Stock: {}, Quantity: {}, Price: {}",
                user.getId(), request.getStockCode(), request.getQuantity(), request.getPrice());

        Account account = accountService.getAccountByUserId(user.getId());
        Order order = orderService.createOrder(
                account,
                request.getStockCode(),
                OrderType.SELL,
                user,
                request.getQuantity(),
                request.getPrice()
        );

        return commonResponse.success(new OrderResponseDto(order));
    }

    @Operation(summary = "주문 취소", description = "미체결된 주문을 취소합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "주문 취소 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (이미 체결된 주문 등)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 주문"
            )
    })
    @PostMapping("/order/{orderId}/cancel")
    public commonResponse<OrderResponseDto> cancelOrder(
            @AuthenticationPrincipal User user,
            @PathVariable Long orderId) {
        log.info("Cancel Order Request - UserId: {}, OrderId: {}", user.getId(), orderId);

        Account account = accountService.getAccountByUserId(user.getId());
        Order cancelledOrder = orderService.cancelOrder(account, orderId);
        return commonResponse.success(new OrderResponseDto(cancelledOrder));
    }

    @Operation(summary = "주문 내역 조회", description = "사용자의 주문 내역을 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공"
            )
    })
    @GetMapping("/orders")
    public commonResponse<Page<OrderResponseDto>> getOrders(
            @AuthenticationPrincipal User user,
            @Parameter(description = "주문 상태")
            @RequestParam(required = false) OrderStatus status,
            @Parameter(description = "시작 일시")
            @RequestParam(required = false) LocalDateTime startTime,
            @Parameter(description = "종료 일시")
            @RequestParam(required = false) LocalDateTime endTime,
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 기준 (createdAt,desc 등)")
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        log.info("Get Orders Request - UserId: {}, Status: {}, Start: {}, End: {}, Page: {}, Size: {}",
                user.getId(), status, startTime, endTime, page, size);

        Account account = accountService.getAccountByUserId(user.getId());

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.fromString(sort.split(",")[1]), sort.split(",")[0])
        );

        Page<Order> orders = orderService.getOrders(account, status, startTime, endTime, pageable);
        return commonResponse.success(orders.map(OrderResponseDto::new));
    }

    @Operation(summary = "보유 종목 조회", description = "사용자의 보유 종목 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공"
            )
    })
    @GetMapping("/holdings")
    public commonResponse<Page<UserStockResponseDto>> getHoldings(
            @AuthenticationPrincipal User user,
            @Parameter(description = "수익률 기준 정렬")
            @RequestParam(defaultValue = "false") boolean sortByGainRate,
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "20") int size) {

        log.info("Get Holdings Request - UserId: {}, SortByGainRate: {}, Page: {}, Size: {}",
                user.getId(), sortByGainRate, page, size);

        Account account = accountService.getAccountByUserId(user.getId());
        Pageable pageable = PageRequest.of(page, size);

        Page<UserStock> holdings = orderService.getHoldings(account, sortByGainRate, pageable);
        return commonResponse.success(holdings.map(UserStockResponseDto::new));
    }
}
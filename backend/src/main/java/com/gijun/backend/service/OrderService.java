package com.gijun.backend.service;

import com.gijun.backend.domain.dto.account.AccountStatus;
import com.gijun.backend.domain.dto.order.OrderStatus;
import com.gijun.backend.domain.dto.order.OrderType;
import com.gijun.backend.domain.entity.*;
import com.gijun.backend.exception.BusinessException;
import com.gijun.backend.exception.ErrorCode;
import com.gijun.backend.repository.OrderRepository;
import com.gijun.backend.repository.StockRepository;
import com.gijun.backend.repository.UserStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final StockRepository stockRepository;
    private final UserStockRepository userStockRepository;

    @Transactional
    public Order createOrder(Account account, String stockCode, OrderType orderType,
                             int quantity, BigDecimal price) {
        // 계좌 상태 확인
        validateAccountStatus(account);

        // 주식 존재 여부 확인
        Stock stock = stockRepository.findByStockCode(stockCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.STOCK_NOT_FOUND));

        // 거래 가능 시간 확인
        validateTradingHours();

        // 주문 수량 및 가격 유효성 검증
        validateOrder(stock, quantity, price);

        // 계좌 잔고 확인 (매수 시)
        if (orderType == OrderType.BUY) {
            validateBuyingPower(account, quantity, price);
        }

        // 보유 수량 확인 (매도 시)
        if (orderType == OrderType.SELL) {
            validateStockHolding(account, stock, quantity);
        }

        // 주문 생성
        Order order = Order.builder()
                .account(account)
                .stock(stock)
                .orderType(orderType)
                .quantity(quantity)
                .orderPrice(price)
                .build();

        order = orderRepository.save(order);

        // 즉시 주문 실행
        executeOrder(order);

        return order;
    }

    @Transactional
    public void executeOrder(Order order) {
        try {
            if (order.getOrderType() == OrderType.BUY) {
                executeBuyOrder(order);
            } else {
                executeSellOrder(order);
            }
            order.complete();
            order.getAccount().updateLastTradeAt();
        } catch (Exception e) {
            order.fail(e.getMessage());
            throw e;
        }
    }

    @Transactional
    public Order cancelOrder(Account account, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // 주문 소유자 확인
        if (!order.getAccount().getId().equals(account.getId())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        // 주문 상태 확인
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException(ErrorCode.ORDER_ALREADY_PROCESSED);
        }

        order.cancel("사용자 취소");
        return orderRepository.save(order);
    }

    private void executeBuyOrder(Order order) {
        // 계좌에서 금액 차감
        BigDecimal newBalance = order.getAccount().getBalance().subtract(order.getTotalAmount());
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_BALANCE);
        }
        order.getAccount().updateBalance(newBalance);

        // 주식 보유 정보 업데이트
        UserStock userStock = userStockRepository
                .findByAccountAndStock(order.getAccount(), order.getStock())
                .orElse(null);

        if (userStock == null) {
            userStock = UserStock.builder()
                    .account(order.getAccount())
                    .stock(order.getStock())
                    .quantity(order.getQuantity())
                    .averagePrice(order.getOrderPrice())
                    .build();
        } else {
            // 평균 단가 계산
            BigDecimal totalCost = userStock.getAveragePrice()
                    .multiply(BigDecimal.valueOf(userStock.getQuantity()))
                    .add(order.getTotalAmount());
            int totalQuantity = userStock.getQuantity() + order.getQuantity();

            BigDecimal newAveragePrice = totalCost.divide(BigDecimal.valueOf(totalQuantity), 2, RoundingMode.HALF_UP);
            UserStock updatedStock = UserStock.builder()
                    .account(userStock.getAccount())
                    .stock(userStock.getStock())
                    .quantity(totalQuantity)
                    .averagePrice(newAveragePrice)
                    .build();

            userStock = updatedStock;
        }

        // 현재가로 평가금액 업데이트
        userStock.updateValue(order.getStock().getCurrentPrice());
        userStockRepository.save(userStock);
    }

    private void executeSellOrder(Order order) {
        // 보유 주식 수량 감소
        UserStock userStock = userStockRepository
                .findByAccountAndStock(order.getAccount(), order.getStock())
                .orElseThrow(() -> new BusinessException(ErrorCode.STOCK_NOT_HELD));

        int remainingQuantity = userStock.getQuantity() - order.getQuantity();
        if (remainingQuantity > 0) {
            UserStock updatedStock = UserStock.builder()
                    .account(userStock.getAccount())
                    .stock(userStock.getStock())
                    .quantity(remainingQuantity)
                    .averagePrice(userStock.getAveragePrice())
                    .build();
            updatedStock.updateValue(order.getStock().getCurrentPrice());
            userStockRepository.save(updatedStock);
        } else if (remainingQuantity == 0) {
            userStockRepository.delete(userStock);
        } else {
            throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK_QUANTITY);
        }

        // 계좌에 금액 추가
        BigDecimal newBalance = order.getAccount().getBalance().add(order.getTotalAmount());
        order.getAccount().updateBalance(newBalance);
    }

    private void validateAccountStatus(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.ACCOUNT_NOT_ACTIVE);
        }
    }


    private void validateTradingHours() {
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        int minute = now.getMinute();
        int dayOfWeek = now.getDayOfWeek().getValue();

        // 주말 체크
//        if (dayOfWeek == 6 || dayOfWeek == 7) {
//            throw new BusinessException(ErrorCode.OUTSIDE_TRADING_HOURS);
//        }

//        // 거래 시간 체크 (9:00 - 17:30)
//        if (hour < 9 || (hour == 17 && minute > 30) || hour > 17) {
//            throw new BusinessException(ErrorCode.OUTSIDE_TRADING_HOURS);
//        }
    }

    private void validateOrder(Stock stock, int quantity, BigDecimal price) {
        // 최소 거래 단위 확인 (10주 단위)
        if (quantity <= 0 || quantity % 10 != 0) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_QUANTITY);
        }

        // 가격 범위 확인 (현재가의 ±15% 이내)
        BigDecimal currentPrice = stock.getCurrentPrice();
        BigDecimal maxPrice = currentPrice.multiply(BigDecimal.valueOf(1.15));
        BigDecimal minPrice = currentPrice.multiply(BigDecimal.valueOf(0.85));

        if (price.compareTo(maxPrice) > 0 || price.compareTo(minPrice) < 0) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_PRICE);
        }
    }

    public Page<Order> getOrders(Account account, OrderStatus status, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        if (startTime != null && endTime != null) {
            return orderRepository.findAccountOrdersInPeriod(account, startTime, endTime, pageable);
        } else if (status != null) {
            return orderRepository.findByAccountAndStatusOrderByCreatedAtDesc(account, status, pageable);
        } else {
            return orderRepository.findByAccountOrderByCreatedAtDesc(account, pageable);
        }
    }

    public Page<UserStock> getHoldings(Account account, boolean sortByGainRate, Pageable pageable) {
        if (sortByGainRate) {
            return userStockRepository.findByAccountOrderByGainRateDesc(account, pageable);
        }
        return userStockRepository.findByAccount(account, pageable);
    }

    private void validateBuyingPower(Account account, int quantity, BigDecimal price) {
        BigDecimal requiredAmount = price.multiply(BigDecimal.valueOf(quantity));
        BigDecimal balance = account.getBalance();

        if (balance.compareTo(requiredAmount) < 0) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_BALANCE);
        }
    }

    private void validateStockHolding(Account account, Stock stock, int quantity) {
        UserStock userStock = userStockRepository
                .findByAccountAndStock(account, stock)
                .orElseThrow(() -> new BusinessException(ErrorCode.STOCK_NOT_HELD));

        if (userStock.getQuantity() < quantity) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK_QUANTITY);
        }
    }
}
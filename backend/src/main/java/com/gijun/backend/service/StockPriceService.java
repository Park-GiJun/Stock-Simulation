package com.gijun.backend.service;

import com.gijun.backend.domain.entity.Stock;
import com.gijun.backend.repository.StockRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@Transactional
@Slf4j
public class StockPriceService {

    private final StockRepository stockRepository;
    private final Random random = new Random();

    public StockPriceService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public void updateAllStockPrices() {
        List<Stock> stocks = stockRepository.findAllByIsTrading(true);

        for (Stock stock : stocks) {
            try {
                updateStockPrice(stock);
            } catch (Exception e) {
                log.error("Error updating price for stock {}: {}", stock.getStockCode(), e.getMessage());
            }
        }
    }

    private void updateStockPrice(Stock stock) {
        // 기준가 대비 변동폭 제한 (일일 상/하한가 ±30%)
        BigDecimal maxPrice = stock.getBasePrice().multiply(BigDecimal.valueOf(1.3));
        BigDecimal minPrice = stock.getBasePrice().multiply(BigDecimal.valueOf(0.7));

        // 현재가 기준 변동폭 (최대 ±2%)
        double changeRate = (random.nextDouble() * 4 - 2) / 100; // -2% ~ +2%

        // 추세 반영 (현재가가 기준가보다 높으면 상승확률 증가, 낮으면 하락확률 증가)
        if (stock.getCurrentPrice().compareTo(stock.getBasePrice()) > 0) {
            changeRate = adjustForTrend(changeRate, -0.3); // 하락 쪽으로 약간 치우치게
        } else if (stock.getCurrentPrice().compareTo(stock.getBasePrice()) < 0) {
            changeRate = adjustForTrend(changeRate, 0.3); // 상승 쪽으로 약간 치우치게
        }

        // 새로운 가격 계산
        BigDecimal currentPrice = stock.getCurrentPrice();
        BigDecimal priceChange = currentPrice.multiply(BigDecimal.valueOf(changeRate));
        BigDecimal newPrice = currentPrice.add(priceChange);

        // 가격 범위 제한
        newPrice = newPrice.max(minPrice).min(maxPrice);

        // 호가단위로 조정
        newPrice = adjustToTickSize(newPrice);

        // 거래량 생성
        int volumeChange = generateVolumeChange(stock, Math.abs(changeRate));

        // 가격 업데이트
        stock.updatePrice(newPrice, volumeChange);
        stockRepository.save(stock);

        log.info("Updated stock price - Code: {}, Price: {}, Volume: {}",
                stock.getStockCode(), newPrice, volumeChange);
    }

    private double adjustForTrend(double changeRate, double bias) {
        return changeRate + (random.nextDouble() * bias);
    }

    private BigDecimal adjustToTickSize(BigDecimal price) {
        // 호가단위 적용
        long tickSize;
        if (price.compareTo(BigDecimal.valueOf(2000)) < 0) {
            tickSize = 1;
        } else if (price.compareTo(BigDecimal.valueOf(5000)) < 0) {
            tickSize = 5;
        } else if (price.compareTo(BigDecimal.valueOf(20000)) < 0) {
            tickSize = 10;
        } else if (price.compareTo(BigDecimal.valueOf(50000)) < 0) {
            tickSize = 50;
        } else if (price.compareTo(BigDecimal.valueOf(200000)) < 0) {
            tickSize = 100;
        } else if (price.compareTo(BigDecimal.valueOf(500000)) < 0) {
            tickSize = 500;
        } else {
            tickSize = 1000;
        }

        long priceInLong = price.longValue();
        return BigDecimal.valueOf((priceInLong / tickSize) * tickSize);
    }

    private int generateVolumeChange(Stock stock, double priceChangeRate) {
        // 기본 거래량 범위 설정
        int baseVolume = random.nextInt(1000) + 100; // 기본 100~1100주

        // 가격 변동이 클수록 거래량 증가
        double volumeMultiplier = 1 + (priceChangeRate * 50); // 가격변동 1%당 거래량 50% 증가

        // 추가 거래량 생성 (거래량 스파이크 발생 가능)
        if (random.nextDouble() < 0.1) { // 10% 확률로 거래량 스파이크
            volumeMultiplier *= (random.nextDouble() * 3 + 2); // 2~5배 거래량 스파이크
        }

        return (int) (baseVolume * volumeMultiplier);
    }

    @Scheduled(cron = "0 0 9 * * MON-FRI") // 평일 아침 9시에 실행
    @Transactional
    public void resetDailyPrices() {
        List<Stock> stocks = stockRepository.findAll();
        for (Stock stock : stocks) {
            stock.resetDaily();
            stockRepository.save(stock);
        }
        log.info("Reset daily stock prices at {}", LocalDateTime.now());
    }
}
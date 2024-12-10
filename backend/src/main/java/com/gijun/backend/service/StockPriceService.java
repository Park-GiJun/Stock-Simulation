package com.gijun.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gijun.backend.domain.entity.Stock;
import com.gijun.backend.domain.dto.websocket.StockPriceMessage;
import com.gijun.backend.repository.StockRepository;
import com.gijun.backend.repository.StockDataRedisRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
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
    private final StockDataRedisRepository redisRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final StockWebSocketService webSocketService;
    private final Random random = new Random();

    private static final String KAFKA_TOPIC = "stock-price-updates";

    public StockPriceService(
            StockRepository stockRepository,
            StockDataRedisRepository redisRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            StockWebSocketService webSocketService) {
        this.stockRepository = stockRepository;
        this.redisRepository = redisRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.webSocketService = webSocketService;
    }

    public void updateAllStockPrices() {
        log.debug("Updating all stock prices");

        List<Stock> stocks = stockRepository.findAllByIsTrading(true);

        for (Stock stock : stocks) {
            try {
                log.debug("Updating price for stock: {}", stock.getStockCode());
                updateStockPrice(stock);
            } catch (Exception e) {
                log.error("Error updating price for stock {}: {}", stock.getStockCode(), e.getMessage());
            }
        }
    }


    private void updateStockPrice(Stock stock) {
        // 이전 가격 저장
        BigDecimal previousPrice = stock.getCurrentPrice();

        // 기존 가격 업데이트 로직
        BigDecimal maxPrice = stock.getBasePrice().multiply(BigDecimal.valueOf(1.3));
        BigDecimal minPrice = stock.getBasePrice().multiply(BigDecimal.valueOf(0.7));

        double changeRate = (random.nextDouble() * 4 - 2) / 100;

        if (stock.getCurrentPrice().compareTo(stock.getBasePrice()) > 0) {
            changeRate = adjustForTrend(changeRate, -0.3);
        } else if (stock.getCurrentPrice().compareTo(stock.getBasePrice()) < 0) {
            changeRate = adjustForTrend(changeRate, 0.3);
        }

        BigDecimal currentPrice = stock.getCurrentPrice();
        BigDecimal priceChange = currentPrice.multiply(BigDecimal.valueOf(changeRate));
        BigDecimal newPrice = currentPrice.add(priceChange);
        newPrice = newPrice.max(minPrice).min(maxPrice);
        newPrice = adjustToTickSize(newPrice);

        int volumeChange = generateVolumeChange(stock, Math.abs(changeRate));

        // DB 업데이트
        stock.updatePrice(newPrice, volumeChange);
        stockRepository.save(stock);

        // StockPriceMessage 생성
        StockPriceMessage priceMessage = StockPriceMessage.builder()
                .stockCode(stock.getStockCode())
                .companyName(stock.getCompanyName())
                .currentPrice(newPrice)
                .previousPrice(previousPrice)
                .volume(Long.valueOf(stock.getDailyVolume()))
                .timestamp(LocalDateTime.now().toString())
                .build();

        // Redis에 저장
        redisRepository.saveStockPrice(priceMessage);

        // Kafka로 메시지 전송
        try {
            kafkaTemplate.send(KAFKA_TOPIC, stock.getStockCode(), toJson(priceMessage));
        } catch (Exception e) {
            log.error("Failed to send message to Kafka for stock {}: {}", stock.getStockCode(), e.getMessage());
        }

        // WebSocket으로 실시간 전송
//        webSocketService.sendStockUpdate(priceMessage);

        log.debug("Updated stock price - Code: {}, Price: {}, Volume: {}",
                stock.getStockCode(), newPrice, volumeChange);
    }

    private double adjustForTrend(double changeRate, double bias) {
        return changeRate + (random.nextDouble() * bias);
    }

    private BigDecimal adjustToTickSize(BigDecimal price) {
        // 기존 코드 유지
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
        // 기존 코드 유지
        int baseVolume = random.nextInt(1000) + 100;
        double volumeMultiplier = 1 + (priceChangeRate * 50);

        if (random.nextDouble() < 0.1) {
            volumeMultiplier *= (random.nextDouble() * 3 + 2);
        }

        return (int) (baseVolume * volumeMultiplier);
    }

    // JSON 변환 유틸리티 메서드
    private String toJson(Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }
}
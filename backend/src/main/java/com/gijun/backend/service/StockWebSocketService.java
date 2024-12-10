package com.gijun.backend.service;

import com.gijun.backend.domain.dto.websocket.StockPriceMessage;
import com.gijun.backend.repository.StockDataRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockWebSocketService {
    private final SimpMessagingTemplate messagingTemplate;
    private final StockDataRedisRepository redisRepository;

    /**
     * WebSocket을 통해 주식 가격 업데이트를 클라이언트에게 전송
     */
    public void sendStockUpdate(StockPriceMessage stockPrice) {
        try {
            log.debug("Attempting to send stock update - Code: {}, Price: {}",
                    stockPrice.getStockCode(), stockPrice.getCurrentPrice());

            messagingTemplate.convertAndSend(
                    "/topic/stocks/" + stockPrice.getStockCode(),
                    stockPrice
            );

            messagingTemplate.convertAndSend(
                    "/topic/stocks/all",
                    stockPrice
            );

            log.debug("Sent stock update via WebSocket - Code: {}, Price: {}",
                    stockPrice.getStockCode(), stockPrice.getCurrentPrice());
        } catch (Exception e) {
            log.error("Error sending stock update via WebSocket: {}", e.getMessage());
        }
    }

    /**
     * 특정 종목의 최신 가격 정보를 조회하여 전송
     */
    public void sendLatestStockPrice(String stockCode) {
        try {
            StockPriceMessage latestPrice = redisRepository.getStockPrice(stockCode);
            if (latestPrice != null) {
                messagingTemplate.convertAndSend(
                        "/topic/stocks/" + stockCode,
                        latestPrice
                );
            }
        } catch (Exception e) {
            log.error("Error sending latest stock price for {}: {}", stockCode, e.getMessage());
        }
    }

    /**
     * 클라이언트별 맞춤 주식 정보 전송
     */
    public void sendCustomStockUpdate(String sessionId, StockPriceMessage stockPrice) {
        try {
            messagingTemplate.convertAndSendToUser(
                    sessionId,
                    "/topic/stocks/custom",
                    stockPrice
            );
        } catch (Exception e) {
            log.error("Error sending custom stock update to session {}: {}", sessionId, e.getMessage());
        }
    }

    public void broadcastAllStockPrices() {
        try {
            List<StockPriceMessage> latestPrices = redisRepository.getAllStockPrices();
            if (latestPrices.isEmpty()) {
                log.warn("No stock prices found in Redis to broadcast.");
                return;
            }

            for (StockPriceMessage priceMessage : latestPrices) {
                sendStockUpdate(priceMessage);
            }
        } catch (Exception e) {
            log.error("Error broadcasting all stock prices: {}", e.getMessage());
        }
    }
}
package com.gijun.backend.scheduler.stock;

import com.gijun.backend.domain.dto.websocket.StockPriceMessage;
import com.gijun.backend.repository.StockDataRedisRepository;
import com.gijun.backend.service.StockWebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
@EnableScheduling
@Slf4j
public class StockPriceBroadcastScheduler {

    private final StockDataRedisRepository redisRepository;
    private final StockWebSocketService webSocketService;
    private final Clock clock;

    public StockPriceBroadcastScheduler(StockDataRedisRepository redisRepository,
                                        StockWebSocketService webSocketService,
                                        Clock clock) {
        this.redisRepository = redisRepository;
        this.webSocketService = webSocketService;
        this.clock = clock;
    }

    @Scheduled(fixedRate = 10000) // 10초마다 실행
    public void broadcastLatestStockPrices() {
        log.info("Broadcasting latest stock prices at {}", LocalDateTime.now(clock));
        try {
            List<StockPriceMessage> latestPrices = redisRepository.getAllStockPrices();
            if (latestPrices.isEmpty()) {
                log.warn("No stock prices found in Redis to broadcast.");
                return;
            }

            for (StockPriceMessage priceMessage : latestPrices) {
                webSocketService.sendStockUpdate(priceMessage);
            }
        } catch (Exception e) {
            log.error("Error broadcasting stock prices: {}", e.getMessage());
        }
    }
}

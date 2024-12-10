package com.gijun.backend.repository;

import com.gijun.backend.domain.dto.websocket.StockPriceMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;

@Slf4j
@Repository
@RequiredArgsConstructor
public class StockDataRedisRepository {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String KEY_PREFIX = "stock:price:";
    private static final String HISTORY_PREFIX = "stock:history:";
    private static final long CACHE_DURATION = 24; // 24시간
    private static final int MAX_HISTORY_SIZE = 100;



    public void saveStockPrice(StockPriceMessage stockPrice) {
        try {
            String key = KEY_PREFIX + stockPrice.getStockCode();
            redisTemplate.opsForValue().set(key, stockPrice, CACHE_DURATION, TimeUnit.HOURS);

            // 가격 히스토리 저장
            String historyKey = HISTORY_PREFIX + stockPrice.getStockCode();
            redisTemplate.opsForList().leftPush(historyKey, stockPrice);
            redisTemplate.opsForList().trim(historyKey, 0, MAX_HISTORY_SIZE - 1);

            log.debug("Saved stock price to Redis: {}", stockPrice);
        } catch (Exception e) {
            log.error("Error saving stock price to Redis: {}", e.getMessage());
        }
    }

    public StockPriceMessage getStockPrice(String stockCode) {
        try {
            String key = KEY_PREFIX + stockCode;
            Object value = redisTemplate.opsForValue().get(key);

            if (value != null) {
                if (value instanceof StockPriceMessage) {
                    return (StockPriceMessage) value;
                } else {
                    return objectMapper.convertValue(value, StockPriceMessage.class);
                }
            }
        } catch (Exception e) {
            log.error("Error getting stock price from Redis for code {}: {}", stockCode, e.getMessage());
        }
        return null;
    }

    public List<StockPriceMessage> getStockPriceHistory(String stockCode) {
        try {
            String historyKey = HISTORY_PREFIX + stockCode;
            List<Object> history = redisTemplate.opsForList().range(historyKey, 0, -1);

            if (history != null) {
                List<StockPriceMessage> result = new ArrayList<>();
                for (Object item : history) {
                    if (item instanceof StockPriceMessage) {
                        result.add((StockPriceMessage) item);
                    } else {
                        result.add(objectMapper.convertValue(item, StockPriceMessage.class));
                    }
                }
                return result;
            }
        } catch (Exception e) {
            log.error("Error getting stock price history from Redis for code {}: {}", stockCode, e.getMessage());
        }
        return new ArrayList<>();
    }

    public void deleteStockPrice(String stockCode) {
        try {
            String key = KEY_PREFIX + stockCode;
            String historyKey = HISTORY_PREFIX + stockCode;
            redisTemplate.delete(key);
            redisTemplate.delete(historyKey);
        } catch (Exception e) {
            log.error("Error deleting stock price from Redis for code {}: {}", stockCode, e.getMessage());
        }
    }

    public void clearAllStockPrices() {
        try {
            redisTemplate.delete(redisTemplate.keys(KEY_PREFIX + "*"));
            redisTemplate.delete(redisTemplate.keys(HISTORY_PREFIX + "*"));
        } catch (Exception e) {
            log.error("Error clearing all stock prices from Redis: {}", e.getMessage());
        }
    }

    public List<StockPriceMessage> getAllStockPrices() {
        try {
            List<Object> values = redisTemplate.opsForValue().multiGet(redisTemplate.keys(KEY_PREFIX + "*"));
            List<StockPriceMessage> result = new ArrayList<>();

            if (values != null) {
                for (Object value : values) {
                    if (value instanceof StockPriceMessage) {
                        result.add((StockPriceMessage) value);
                    } else {
                        result.add(objectMapper.convertValue(value, StockPriceMessage.class));
                    }
                }
            }

            return result;
        } catch (Exception e) {
            log.error("Error getting all stock prices from Redis: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}
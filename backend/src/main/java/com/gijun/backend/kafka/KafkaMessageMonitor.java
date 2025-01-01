package com.gijun.backend.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.support.KafkaHeaders;

import java.time.LocalDateTime;

@Slf4j
@Component
public class KafkaMessageMonitor {

    private final ObjectMapper objectMapper;

    public KafkaMessageMonitor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = {"auth-topic", "default-topic", "stock-price-updates"},
            groupId = "monitoring-group")
    public void monitorMessages(ConsumerRecord<String, String> record) {
        try {
            String topic = record.topic();
            String value = record.value();
            long offset = record.offset();
            int partition = record.partition();
            LocalDateTime timestamp = LocalDateTime.now();

            // JSON 포맷팅
            Object jsonMsg = objectMapper.readValue(value, Object.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(jsonMsg);

            log.info("\n=== Kafka Message ===\n" +
                            "Timestamp: {}\n" +
                            "Topic: {}\n" +
                            "Partition: {}\n" +
                            "Offset: {}\n" +
                            "Content:\n{}\n" +
                            "===================\n",
                    timestamp, topic, partition, offset, prettyJson);

        } catch (Exception e) {
            log.error("Error processing message: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "auth-topic", groupId = "auth-monitor")
    public void monitorAuthMessages(@Payload String message) {
        try {
            Object jsonMsg = objectMapper.readValue(message, Object.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(jsonMsg);
            log.info("\n=== Auth Message ===\n{}\n===================\n", prettyJson);
        } catch (Exception e) {
            log.error("Error processing auth message: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "stock-price-updates", groupId = "stock-monitor")
    public void monitorStockUpdates(@Payload String message) {
        try {
            Object jsonMsg = objectMapper.readValue(message, Object.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(jsonMsg);
            log.info("\n=== Stock Price Update ===\n{}\n===================\n", prettyJson);
        } catch (Exception e) {
            log.error("Error processing stock update message: {}", e.getMessage());
        }
    }
}
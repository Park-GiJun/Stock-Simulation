package com.gijun.backend.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
public class KafkaHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        Properties config = new Properties();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "15.164.142.172:9092");
        config.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "5000");
        config.put(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, "5000");

        try (AdminClient client = AdminClient.create(config)) {
            log.info("Attempting to connect to Kafka broker at 15.164.142.172:9092");
            var topics = client.listTopics().names().get(5, TimeUnit.SECONDS);
            log.info("Successfully connected to Kafka broker. Available topics: {}", topics);
            return Health.up()
                    .withDetail("bootstrapServers", "15.164.142.172:9092")
                    .withDetail("availableTopics", topics)
                    .build();
        } catch (Exception e) {
            log.error("Failed to connect to Kafka broker: {}", e.getMessage(), e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("bootstrapServers", "15.164.142.172:9092")
                    .build();
        }
    }
}
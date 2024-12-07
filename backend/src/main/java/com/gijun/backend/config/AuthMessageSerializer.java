package com.gijun.backend.config.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gijun.backend.domain.dto.kafka.auth.AuthMessage;
import org.apache.kafka.common.serialization.Serializer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthMessageSerializer implements Serializer<AuthMessage> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(String topic, AuthMessage data) {
        try {
            return objectMapper.writeValueAsBytes(data);
        } catch (Exception e) {
            log.error("Error serializing AuthMessage: ", e);
            throw new RuntimeException("Error serializing AuthMessage", e);
        }
    }
}
package com.gijun.backend.domain.dto.kafka.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gijun.backend.utils.JsonUtils;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthMessage {
    private String messageId;
    private String messageType;
    private String payload;

    public static AuthMessage of(String messageType, Object payload, JsonUtils jsonUtils) {
        return AuthMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .messageType(messageType)
                .payload(jsonUtils.toJson(payload))
                .build();
    }
}
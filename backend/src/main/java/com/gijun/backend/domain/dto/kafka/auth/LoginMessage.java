package com.gijun.backend.domain.dto.kafka.auth;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class LoginMessage {
    private String userId;
    private String username;
    private String ipAddress;
    private LocalDateTime loginTime;
    private boolean success;
    private String errorMessage;
}
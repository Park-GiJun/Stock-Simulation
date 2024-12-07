package com.gijun.backend.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;  // 액세스 토큰 만료 시간 (ms)

    @Value("${jwt.refreshExpiration}")
    private Long refreshExpiration;  // 리프레시 토큰 만료 시간 (ms)

    @Value("${jwt.header}")
    private String header;

    @Value("${jwt.prefix}")
    private String prefix;
}

package com.gijun.backend.security;

import com.gijun.backend.config.JwtConfig;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtConfig jwtConfig;
    private final UserDetailsService userDetailsService;
    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes());
    }

    // 액세스 토큰 생성
    public String createAccessToken(String username) {
        Instant now = Instant.now();
        Instant validity = now.plusMillis(jwtConfig.getExpiration());  // 액세스 토큰 유효 기간

        return Jwts.builder()
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(validity))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    // 리프레시 토큰 생성
    public String createRefreshToken(String username) {
        Instant now = Instant.now();
        Instant validity = now.plusMillis(jwtConfig.getRefreshExpiration());  // 리프레시 토큰 유효 기간

        return Jwts.builder()
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(validity))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    // 토큰에서 사용자 이름 추출
    public String getUsername(String token) {
        return Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // 인증 객체 생성
    public Authentication getAuthentication(String token) {
        String username = getUsername(token);
        log.info("Extracted username from token: {}", username);

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (userDetails instanceof CustomUserDetails customUserDetails) {
            log.info("CustomUserDetails: {}", customUserDetails);
        }

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }


    // 액세스 토큰 재발급 (리프레시 토큰을 사용할 때)
    public String refreshAccessToken(String refreshToken) {
        if (validateToken(refreshToken)) {
            String username = getUsername(refreshToken);
            return createAccessToken(username);  // 유효한 리프레시 토큰을 사용하여 새로운 액세스 토큰 생성
        }
        throw new JwtException("Invalid refresh token");
    }
}

package com.gijun.backend.service;


import com.gijun.backend.config.JwtConfig;
import com.gijun.backend.domain.dto.auth.LoginDto;
import com.gijun.backend.domain.dto.auth.SignupDto;
import com.gijun.backend.domain.dto.common.commonResponse;
import com.gijun.backend.domain.dto.kafka.auth.AuthMessage;
import com.gijun.backend.domain.dto.kafka.auth.LoginMessage;
import com.gijun.backend.domain.entity.User;
import com.gijun.backend.exception.AuthenticationException;
import com.gijun.backend.exception.ErrorCode;
import com.gijun.backend.repository.UserRepository;
import com.gijun.backend.security.JwtTokenProvider;
import com.gijun.backend.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final KafkaTemplate<String, String> kafkaTemplate;  // String으로 변경
    private final JsonUtils jsonUtils;
    private static final String AUTH_TOPIC = "auth-topic";
    private final RedisTemplate<String, String> redisTemplate;
    private static final String SESSION_PREFIX = "USER_SESSION:";
    private final JwtConfig jwtConfig;

    public commonResponse<LoginDto.LoginResponse> login(LoginDto.LoginRequest request, String ipAddress) {
        try {
            User user = userRepository.findByUserId(request.getUserId())
                    .orElseThrow(() -> new AuthenticationException(ErrorCode.INVALID_CREDENTIALS));

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new AuthenticationException(ErrorCode.INVALID_CREDENTIALS);
            }

            // 기존 세션 확인 및 삭제
            ValueOperations<String, String> ops = redisTemplate.opsForValue();
            String existingSession = ops.get(SESSION_PREFIX + user.getUserId());
            if (existingSession != null) {
                redisTemplate.delete(SESSION_PREFIX + user.getUserId()); // 기존 세션 제거
            }

            // 새로운 세션 생성
            String accessToken = jwtTokenProvider.createAccessToken(user.getUserId());
            String refreshToken = jwtTokenProvider.createRefreshToken(user.getUserId());
            ops.set(SESSION_PREFIX + user.getUserId(), accessToken, jwtConfig.getExpiration(), TimeUnit.MILLISECONDS);

            user.updateLastLogin();

            LoginMessage loginMessage = LoginMessage.builder()
                    .userId(user.getUserId())
                    .username(user.getUsername())
                    .ipAddress(ipAddress)
                    .loginTime(LocalDateTime.now())
                    .success(true)
                    .build();

            sendAuthMessage("LOGIN", loginMessage);

            return commonResponse.success(new LoginDto.LoginResponse(accessToken, refreshToken));
        } catch (Exception e) {
            handleLoginFailure(request, ipAddress, e);
            throw e;
        }
    }

    private void handleLoginFailure(LoginDto.LoginRequest request, String ipAddress, Exception e) {
        // 실패 메시지 생성
        LoginMessage failureMessage = LoginMessage.builder()
                .userId(request.getUserId())
                .ipAddress(ipAddress)
                .loginTime(LocalDateTime.now())
                .success(false)
                .errorMessage(e.getMessage())
                .build();

        // Kafka에 메시지 전송
        sendAuthMessage("LOGIN_FAILURE", failureMessage);

        // 로깅
        log.error("Login failed for user: {} from IP: {}. Error: {}", request.getUserId(), ipAddress, e.getMessage());
    }

    public void logout(String userId) {
        redisTemplate.delete("USER_SESSION:" + userId); // 세션 삭제
        redisTemplate.delete("REFRESH_TOKEN:" + userId); // Refresh Token 삭제
    }



    @Transactional
    public commonResponse<SignupDto.SignupResponse> signup(SignupDto.SignupRequest request, String ipAddress) {
        try {
            // 중복 검사
            if (userRepository.existsByUserId(request.getUserId())) {
                throw new AuthenticationException(ErrorCode.DUPLICATE_USER_ID);
            }

            if (userRepository.existsByEmail(request.getEmail())) {
                throw new AuthenticationException(ErrorCode.DUPLICATE_EMAIL);
            }

            if (userRepository.existsByUsername(request.getUsername())) {
                throw new AuthenticationException(ErrorCode.DUPLICATE_USERNAME);
            }

            // 새로운 사용자 생성
            User newUser = User.builder()
                    .userId(request.getUserId())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .role("ROLE_USER")
                    .createdAt(LocalDateTime.now())  // Add this line
                    .build();

            userRepository.save(newUser);

            // Kafka 메시지 전송
            LoginMessage signupMessage = LoginMessage.builder()
                    .userId(newUser.getUserId())
                    .username(newUser.getUsername())
                    .ipAddress(ipAddress)
                    .loginTime(LocalDateTime.now())
                    .success(true)
                    .build();

            sendAuthMessage("SIGNUP", signupMessage);

            return commonResponse.success(
                    SignupDto.SignupResponse.builder()
                            .userId(newUser.getUserId())
                            .username(newUser.getUsername())
                            .build()
            );

        } catch (Exception e) {
            // 실패 시 Kafka 메시지 전송
            LoginMessage failureMessage = LoginMessage.builder()
                    .userId(request.getUserId())
                    .ipAddress(ipAddress)
                    .loginTime(LocalDateTime.now())
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();

            sendAuthMessage("SIGNUP_FAILURE", failureMessage);
            throw e;
        }
    }


    private void sendAuthMessage(String messageType, Object payload) {
        AuthMessage message = AuthMessage.of(messageType, payload, jsonUtils);
        kafkaTemplate.send(AUTH_TOPIC, jsonUtils.toJson(message));
    }
}

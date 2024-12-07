package com.gijun.backend.controller;


import com.gijun.backend.domain.dto.auth.LoginDto;
import com.gijun.backend.domain.dto.auth.SignupDto;
import com.gijun.backend.domain.dto.common.commonResponse;
import com.gijun.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "인증 관련 API")
@RestController
@RequestMapping("/auth")
@SessionAttributes(types = HttpSession.class)
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;


    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @PostMapping("/signup")
    public commonResponse<SignupDto.SignupResponse> signup(
            @Parameter(description = "회원가입 정보", required = true)
            @Valid @RequestBody SignupDto.SignupRequest request,
            HttpServletRequest servletRequest) {
        log.info("Signup Api");
        String ipAddress = servletRequest.getRemoteAddr();
        return authService.signup(request, ipAddress);
    }

    @Operation(
            summary = "사용자 로그인",
            description = "이메일과 비밀번호를 사용하여 사용자 인증을 수행하고 JWT 토큰을 발급합니다.",
            tags = {"Authentication"}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginDto.LoginResponse.class),
                            examples = @ExampleObject(
                                    value = """
                    {
                      "status": "SUCCESS",
                      "message": "로그인 성공",
                      "data": {
                        "accessToken": "eyJhbGciOiJIUzI1...",
                        "refreshToken": "eyJhbGciOiJIUzI1..."
                      }
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "로그인 실패 - 잘못된 인증 정보",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = commonResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "너무 많은 로그인 시도"
            )
    })
    @PostMapping("/login")
    public commonResponse<LoginDto.LoginResponse> login(
            @Parameter(
                    description = "로그인 요청 정보",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = LoginDto.LoginRequest.class),
                            examples = @ExampleObject(
                                    value = """
                        {
                          "email": "user@example.com",
                          "password": "password123"
                        }
                        """
                            )
                    )
            )
            @Valid @RequestBody LoginDto.LoginRequest loginRequest,
            HttpServletRequest servletRequest) {
        log.info("Login Api");
        String ipAddress = getClientIp(servletRequest);
        return authService.login(loginRequest, ipAddress);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}

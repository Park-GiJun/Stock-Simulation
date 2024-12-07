package com.gijun.backend.aop;

import com.gijun.backend.domain.entity.ApiLog;
import com.gijun.backend.repository.ApiLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.nio.charset.StandardCharsets;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LoggingAspect {

    private final ObjectMapper objectMapper;
    private final ApiLogRepository apiLogRepository;

    @Around("execution(* com.gijun.backend.controller..*.*(..))")
    public Object logAPICall(ProceedingJoinPoint joinPoint) throws Throwable {
        String traceId = UUID.randomUUID().toString();
        StopWatch stopWatch = new StopWatch();

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String className = methodSignature.getDeclaringType().getSimpleName();
        String methodName = methodSignature.getName();

        // 요청 파라미터와 바디 처리
        Map<String, Object> requestData = new HashMap<>();

        // URL 파라미터 처리
        request.getParameterMap().forEach((key, value) -> {
            requestData.put("param_" + key, value.length == 1 ? value[0] : value);
        });

        // POST 메서드인 경우 바디 처리
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            Object[] args = joinPoint.getArgs();
            String[] paramNames = methodSignature.getParameterNames();

            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                // HttpServletRequest나 기타 서블릿 관련 객체 제외
                if (arg != null &&
                        !arg.getClass().getName().startsWith("org.springframework") &&
                        !arg.getClass().getName().startsWith("jakarta.servlet")) {
                    requestData.put("body_" + paramNames[i], arg);
                }
            }
        }

        String requestDataString = objectMapper.writeValueAsString(requestData);

        ApiLog.ApiLogBuilder logBuilder = ApiLog.builder()
                .traceId(traceId)
                .httpMethod(request.getMethod())
                .requestUri(request.getRequestURI())
                .controllerName(className)
                .methodName(methodName)
                .requestParameters(requestDataString)
                .clientIp(getClientIP(request))
                .userAgent(request.getHeader("User-Agent"));

        Object result = null;
        try {
            stopWatch.start();
            result = joinPoint.proceed();
            stopWatch.stop();

            // 성공 로그 저장
            apiLogRepository.save(logBuilder
                    .responseData(objectMapper.writeValueAsString(result))
                    .executionTime(stopWatch.getTotalTimeMillis())
                    .success(true)
                    .build());

            return result;
        } catch (Exception e) {
            // 실패 로그 저장
            apiLogRepository.save(logBuilder
                    .executionTime(stopWatch.getLastTaskTimeMillis())
                    .errorMessage(e.getMessage())
                    .success(false)
                    .build());

            throw e;
        }
    }

    private String getClientIP(HttpServletRequest request) {
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
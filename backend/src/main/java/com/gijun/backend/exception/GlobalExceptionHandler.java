package com.gijun.backend.exception;

import com.gijun.backend.domain.dto.common.commonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Base Exception 처리
    @ExceptionHandler(BaseException.class)
    protected ResponseEntity<commonResponse<Object>> handleBaseException(BaseException e) {
        log.error("BaseException: {}", e.getMessage());
        return new ResponseEntity<>(commonResponse.error(e.getMessage()), e.getStatus());
    }

    // 유효성 검사 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<commonResponse<Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("MethodArgumentNotValidException: {}", e.getMessage());
        String errorMessage = e.getBindingResult()
                .getAllErrors()
                .get(0)
                .getDefaultMessage();
        return new ResponseEntity<>(commonResponse.error(errorMessage), ErrorCode.INVALID_INPUT_VALUE.getStatus());
    }

    // 인증 예외 처리
    @ExceptionHandler(BadCredentialsException.class)
    protected ResponseEntity<commonResponse<Object>> handleBadCredentialsException(BadCredentialsException e) {
        log.error("BadCredentialsException: {}", e.getMessage());
        return new ResponseEntity<>(commonResponse.error("Invalid credentials"), ErrorCode.INVALID_CREDENTIALS.getStatus());
    }

    // 접근 권한 예외 처리
    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<commonResponse<Object>> handleAccessDeniedException(AccessDeniedException e) {
        log.error("AccessDeniedException: {}", e.getMessage());
        return new ResponseEntity<>(commonResponse.error("Access denied"), ErrorCode.UNAUTHORIZED.getStatus());
    }

    // 바인딩 예외 처리
    @ExceptionHandler(BindException.class)
    protected ResponseEntity<commonResponse<Object>> handleBindException(BindException e) {
        log.error("BindException: {}", e.getMessage());
        String errorMessage = e.getBindingResult()
                .getAllErrors()
                .get(0)
                .getDefaultMessage();
        return new ResponseEntity<>(commonResponse.error(errorMessage), ErrorCode.INVALID_INPUT_VALUE.getStatus());
    }

    // HTTP Method 예외 처리
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<commonResponse<Object>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("HttpRequestMethodNotSupportedException: {}", e.getMessage());
        return new ResponseEntity<>(commonResponse.error("Method not allowed"), ErrorCode.METHOD_NOT_ALLOWED.getStatus());
    }

    // 나머지 모든 예외 처리
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<commonResponse<Object>> handleException(Exception e) {
        log.error("Exception: {}", e.getMessage());
        return new ResponseEntity<>(commonResponse.error("Internal server error"), ErrorCode.INTERNAL_SERVER_ERROR.getStatus());
    }
}
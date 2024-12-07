package com.gijun.backend.exception;

// 인증 관련 예외
public class AuthenticationException extends BaseException {
    public AuthenticationException(ErrorCode errorCode) {
        super(errorCode);
    }
}


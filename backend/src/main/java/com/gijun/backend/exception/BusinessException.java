package com.gijun.backend.exception;

// 비즈니스 로직 예외
public class BusinessException extends BaseException {
    public BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }
}

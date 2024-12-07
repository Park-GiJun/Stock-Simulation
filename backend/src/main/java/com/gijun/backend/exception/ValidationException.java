package com.gijun.backend.exception;

// 유효성 검사 예외
public class ValidationException extends BaseException {
    public ValidationException(ErrorCode errorCode) {
        super(errorCode);
    }
}

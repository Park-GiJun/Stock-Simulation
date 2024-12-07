package com.gijun.backend.exception;

// 시스템 관련 예외
public class SystemException extends BaseException {
    public SystemException(ErrorCode errorCode) {
        super(errorCode);
    }
}

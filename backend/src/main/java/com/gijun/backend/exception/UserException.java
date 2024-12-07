package com.gijun.backend.exception;

// 사용자 관련 예외
public class UserException extends BaseException {
    public UserException(ErrorCode errorCode) {
        super(errorCode);
    }
}

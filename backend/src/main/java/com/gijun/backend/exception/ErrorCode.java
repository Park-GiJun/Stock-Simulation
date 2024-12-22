package com.gijun.backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "Invalid input value"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "Method not allowed"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "Internal server error"),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C004", "Invalid type value"),

    // Authentication
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "Unauthorized"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A002", "Invalid token"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A003", "Expired token"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "A004", "Invalid credentials"),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "User not found"),
    DUPLICATE_USER_ID(HttpStatus.CONFLICT, "U002", "Duplicate user ID"),
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "U003", "Duplicate username"),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "U004", "Duplicate email"),

    // System
    REDIS_CONNECTION_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "S001", "Redis connection error"),
    KAFKA_CONNECTION_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "S002", "Kafka connection error"),
    ELASTICSEARCH_CONNECTION_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "S003", "Elasticsearch connection error"),
    DATABASE_CONNECTION_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "S004", "Database connection error"),

    // User Order
    STOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "UO001", "Stock not found"),
    STOCK_NOT_HELD(HttpStatus.FORBIDDEN, "UO002", "Stock not held"),

    // Stock Time
    OUTSIDE_TRADING_HOURS(HttpStatus.FORBIDDEN, "ST001", "Outside trading hours"),
    MARKET_CLOSED(HttpStatus.FORBIDDEN, "ST002", "Market closed"),

    // Order Validation
    INVALID_ORDER_QUANTITY(HttpStatus.BAD_REQUEST, "OV001", "Invalid order quantity"),
    INVALID_ORDER_PRICE(HttpStatus.BAD_REQUEST, "OV002", "Invalid order price"),
    PRICE_LIMIT_EXCEEDED(HttpStatus.FORBIDDEN, "OV003", "Price limit exceeded"),

    // 잔고 /수량
    INSUFFICIENT_BALANCE(HttpStatus.FORBIDDEN, "BO001", "Insufficient balance"),
    INSUFFICIENT_STOCK_QUANTITY(HttpStatus.FORBIDDEN, "BO002", "Insufficient stock quantity"),

    // 주문 처리
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "O001", "Order not found"),
    ORDER_ALREADY_PROCESSED(HttpStatus.CONFLICT, "O002", "Order already processed"),
    ORDER_ALREADY_CANCELED(HttpStatus.CONFLICT, "O003", "Order already canceled"),
    ORDER_ALREADY_FILLED(HttpStatus.CONFLICT, "O004", "Order already filled"),
    ORDER_NOT_MATCHED(HttpStatus.FORBIDDEN, "O005", "Order not matched"),
    UNAUTHORIZED_ORDER(HttpStatus.FORBIDDEN, "O006", "Unauthorized to view order"),
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "O007", "Unauthorized to access order"),

    // Account
    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "AC001", "Account not found"),
    DUPLICATE_ACCOUNT(HttpStatus.CONFLICT, "AC002", "Duplicate account number"),

    INVALID_ACCOUNT_STATUS(HttpStatus.BAD_REQUEST, "AS001", "Invalid account status"),
    ACCOUNT_NOT_ACTIVE(HttpStatus.FORBIDDEN, "AS002", "Account not active");


    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
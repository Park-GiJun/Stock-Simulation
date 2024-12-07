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
    DATABASE_CONNECTION_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "S004", "Database connection error");


    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
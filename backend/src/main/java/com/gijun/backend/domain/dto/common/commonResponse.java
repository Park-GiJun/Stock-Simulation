package com.gijun.backend.domain.dto.common;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class commonResponse<T> {
    private boolean success;
    private String message;
    private T data;

    @Builder
    public commonResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static <T> commonResponse<T> success(T data) {
        return commonResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static <T> commonResponse<T> error(String message) {
        return commonResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}
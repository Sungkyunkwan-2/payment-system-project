package com.paymentteamproject.common.dto;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
public class ApiResponse<T> {

    private final LocalDateTime timestamp;
    private final boolean success;
    private final HttpStatus code;
    private final String message;
    private final T data;

    public ApiResponse(boolean success, HttpStatus code, String message, T data) {
        this.timestamp = LocalDateTime.now();
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 성공
    public static <T> ApiResponse<T> success(HttpStatus code, String message, T data) {
        return new ApiResponse<>(true, code, message, data);
    }

    // 실패
    public static <T> ApiResponse<T> error(HttpStatus code, String message, T data) {
        return new ApiResponse<>(false, code, message, data);
    }
}

package com.paymentteamproject.config;

import com.paymentteamproject.common.dtos.ApiResponse;
import com.paymentteamproject.common.exception.BusinessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            BusinessException e
    ) {
        return ResponseEntity
                .status(e.getStatus())
                .body(ApiResponse.error(
                        e.getStatus(),
                        e.getMessage(),
                        null
                ));
    }
}

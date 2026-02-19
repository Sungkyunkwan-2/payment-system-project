package com.paymentteamproject.config;

import com.paymentteamproject.common.dto.ApiResponse;
import com.paymentteamproject.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<String>> handleBusinessException(BusinessException e) {
        log.error("Business Error : ", e);
        return ResponseEntity
                .status(e.getStatus())
                .body(ApiResponse.error(
                        e.getStatus(),
                        e.getMessage(),
                        e.getClass().getSimpleName()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleAll(Exception e) {
        log.error("Internal Server Error : ", e);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(status)
                .body(ApiResponse.error(
                        status,
                        "서버 내부 오류가 발생했습니다.",
                        e.getClass().getSimpleName()
                ));
    }
}

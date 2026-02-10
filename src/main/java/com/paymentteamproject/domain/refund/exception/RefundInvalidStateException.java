package com.paymentteamproject.domain.refund.exception;

import com.paymentteamproject.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class RefundInvalidStateException extends BusinessException {
    public RefundInvalidStateException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

    public RefundInvalidStateException(String message, HttpStatus status) {
        super(message, status);
    }
}
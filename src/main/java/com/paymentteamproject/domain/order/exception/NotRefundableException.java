package com.paymentteamproject.domain.order.exception;

import com.paymentteamproject.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class NotRefundableException extends BusinessException {
    public NotRefundableException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}

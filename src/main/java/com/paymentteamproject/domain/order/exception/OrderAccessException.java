package com.paymentteamproject.domain.order.exception;

import com.paymentteamproject.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class OrderAccessException extends BusinessException {
    public OrderAccessException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}

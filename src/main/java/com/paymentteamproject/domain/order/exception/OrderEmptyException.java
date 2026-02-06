package com.paymentteamproject.domain.order.exception;

import com.paymentteamproject.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class OrderEmptyException extends BusinessException {
    public OrderEmptyException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
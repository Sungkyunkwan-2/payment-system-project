package com.paymentteamproject.domain.orderProduct.exception;

import com.paymentteamproject.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class OrderProductEmptyException extends BusinessException {
    public OrderProductEmptyException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
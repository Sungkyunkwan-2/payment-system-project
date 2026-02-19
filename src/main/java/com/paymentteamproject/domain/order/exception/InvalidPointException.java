package com.paymentteamproject.domain.order.exception;

import com.paymentteamproject.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class InvalidPointException extends BusinessException {
    public InvalidPointException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}

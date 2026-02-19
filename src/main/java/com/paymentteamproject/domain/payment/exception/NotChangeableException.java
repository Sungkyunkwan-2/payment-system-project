package com.paymentteamproject.domain.payment.exception;

import com.paymentteamproject.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class NotChangeableException extends BusinessException {
    public NotChangeableException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}

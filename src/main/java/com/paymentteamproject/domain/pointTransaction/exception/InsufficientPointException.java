package com.paymentteamproject.domain.pointTransaction.exception;

import com.paymentteamproject.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class InsufficientPointException extends BusinessException {
    public InsufficientPointException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}

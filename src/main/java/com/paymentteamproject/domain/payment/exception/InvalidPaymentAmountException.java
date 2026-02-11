package com.paymentteamproject.domain.payment.exception;

import com.paymentteamproject.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class InvalidPaymentAmountException extends BusinessException {
    public InvalidPaymentAmountException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}

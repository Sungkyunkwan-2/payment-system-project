package com.paymentteamproject.domain.webhook.exception;

import com.paymentteamproject.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class PaymentAmountMismatchException extends BusinessException {
    public PaymentAmountMismatchException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}

package com.paymentteamproject.domain.payment.exception;

import com.paymentteamproject.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class MinimumPaymentAmountException extends BusinessException {
    public MinimumPaymentAmountException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}

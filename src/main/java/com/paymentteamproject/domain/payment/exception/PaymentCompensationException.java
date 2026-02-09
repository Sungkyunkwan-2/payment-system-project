package com.paymentteamproject.domain.payment.exception;

import com.paymentteamproject.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class PaymentCompensationException extends BusinessException {
    public PaymentCompensationException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

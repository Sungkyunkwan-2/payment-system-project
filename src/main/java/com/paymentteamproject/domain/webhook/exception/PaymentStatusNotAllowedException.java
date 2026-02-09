package com.paymentteamproject.domain.webhook.exception;

import com.paymentteamproject.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class PaymentStatusNotAllowedException extends BusinessException {
    public PaymentStatusNotAllowedException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}

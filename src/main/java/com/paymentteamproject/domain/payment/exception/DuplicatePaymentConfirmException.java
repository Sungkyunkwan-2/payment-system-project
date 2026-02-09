package com.paymentteamproject.domain.payment.exception;

import com.paymentteamproject.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class DuplicatePaymentConfirmException extends BusinessException {
    public DuplicatePaymentConfirmException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}

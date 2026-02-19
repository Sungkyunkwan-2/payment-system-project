package com.paymentteamproject.domain.refund.exception;

import com.paymentteamproject.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class RefundPeriodExpiredException extends BusinessException {
    public RefundPeriodExpiredException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}

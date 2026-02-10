package com.paymentteamproject.domain.refund.exception;

import com.paymentteamproject.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class RefundForbiddenException extends BusinessException {
    public RefundForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
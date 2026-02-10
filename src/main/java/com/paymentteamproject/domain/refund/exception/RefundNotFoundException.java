package com.paymentteamproject.domain.refund.exception;

import com.paymentteamproject.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class RefundNotFoundException extends BusinessException {
    public RefundNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
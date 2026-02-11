package com.paymentteamproject.domain.subscription.exception;

import com.paymentteamproject.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class InvalidCancelSubscriptionException extends BusinessException {
    public InvalidCancelSubscriptionException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}

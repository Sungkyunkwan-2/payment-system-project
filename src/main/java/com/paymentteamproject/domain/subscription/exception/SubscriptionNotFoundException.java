package com.paymentteamproject.domain.subscription.exception;

import com.paymentteamproject.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class SubscriptionNotFoundException extends BusinessException {
    public SubscriptionNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}

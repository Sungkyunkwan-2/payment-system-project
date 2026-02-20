package com.paymentteamproject.domain.subscription.exception;

import com.paymentteamproject.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class SubscriptionAlreadyBilledException extends BusinessException {
    public SubscriptionAlreadyBilledException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}

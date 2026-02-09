package com.paymentteamproject.domain.payment.exeception;

import com.paymentteamproject.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class PaymentNotFoundException extends BusinessException {
    public PaymentNotFoundException(String message) {

        super(message, HttpStatus.NOT_FOUND);
    }
}

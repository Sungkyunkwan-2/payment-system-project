package com.paymentteamproject.domain.pointTransaction.exception;

import com.paymentteamproject.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class InvalidRefundPointAmountException extends BusinessException {
    public InvalidRefundPointAmountException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}

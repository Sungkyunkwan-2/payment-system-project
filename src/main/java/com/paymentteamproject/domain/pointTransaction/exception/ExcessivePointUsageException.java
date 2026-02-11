package com.paymentteamproject.domain.pointTransaction.exception;

import com.paymentteamproject.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class ExcessivePointUsageException extends BusinessException {
    public ExcessivePointUsageException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}

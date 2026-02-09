package com.paymentteamproject.domain.user.exception;

import com.paymentteamproject.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BusinessException {
    public UserNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }

    public UserNotFoundException() {
        super("사용자가 존재하지 않습니다.", HttpStatus.NOT_FOUND);
    }
}

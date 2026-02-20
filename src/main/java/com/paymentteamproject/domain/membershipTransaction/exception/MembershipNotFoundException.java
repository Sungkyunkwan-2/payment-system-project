package com.paymentteamproject.domain.membershipTransaction.exception;

import com.paymentteamproject.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class MembershipNotFoundException extends BusinessException {
    public MembershipNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}

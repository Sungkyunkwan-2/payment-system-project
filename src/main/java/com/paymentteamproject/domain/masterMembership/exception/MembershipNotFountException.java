package com.paymentteamproject.domain.masterMembership.exception;

import com.paymentteamproject.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class MembershipNotFountException extends BusinessException {
    public MembershipNotFountException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}

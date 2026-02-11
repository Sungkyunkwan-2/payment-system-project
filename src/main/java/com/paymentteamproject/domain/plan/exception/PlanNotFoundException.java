package com.paymentteamproject.domain.plan.exception;

import com.paymentteamproject.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class PlanNotFoundException extends BusinessException {
    public PlanNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}

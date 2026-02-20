package com.paymentteamproject.domain.product.exception;

import com.paymentteamproject.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class ProductNotFoundException extends BusinessException {
    public ProductNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}

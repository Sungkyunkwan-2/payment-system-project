package com.paymentteamproject.domain.user.exception;

import com.paymentteamproject.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class DuplicateEmailException extends BusinessException {
    public DuplicateEmailException() {
        super("이미 사용 중인 이메일입니다.", HttpStatus.CONFLICT);
    }
}

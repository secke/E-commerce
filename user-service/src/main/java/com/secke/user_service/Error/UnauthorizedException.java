package com.secke.user_service.Error;

import org.springframework.http.HttpStatus;

// Unauthorized
public class UnauthorizedException extends BaseException {
    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}

package com.sathya.restaurant_management.exception;

/** Wrong credentials / disabled account. Mapped to HTTP 401. */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}

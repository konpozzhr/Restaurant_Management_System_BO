package com.sathya.restaurant_management.exception;

/** Too many failed sign-ins in a short time. Mapped to HTTP 429. */
public class TooManyAttemptsException extends RuntimeException {

    public TooManyAttemptsException(String message) {
        super(message);
    }
}

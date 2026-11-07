package com.sathya.restaurant_management.exception;

/** Business-rule clash (duplicate name, category still in use, bad order state...). Mapped to HTTP 409. */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
